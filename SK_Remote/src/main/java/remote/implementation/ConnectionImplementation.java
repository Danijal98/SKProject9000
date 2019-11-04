package remote.implementation;

import api.documentation.Connection;
import api.documentation.UserPrivilege;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import de.vandermeer.asciitable.AsciiTable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ConnectionImplementation implements Connection {

    private String path;
    private String currentUser;
    private String password;
    private DbxClientV2 client;
    private UserPrivilege currentPrivilege;


    public ConnectionImplementation(String path, String currentUser, String password) {
        this.path = path;
        this.currentUser = currentUser;
        this.password = password;
    }

    public ConnectionImplementation(String path, String currentUser, String password, DbxClientV2 client) {
        this.path = path;
        this.currentUser = currentUser;
        this.password = password;
        this.client = client;
    }

    public boolean upload(String destination, String... paths) {
        try {

            if(destination.equals("/")){
                destination = "";
            }

            if(paths.length > 1){
                File pom = new File(paths[0]);
                String createdName = pom.getName().substring(0,pom.getName().lastIndexOf("."));
                return upload(destination,createdName,paths);
            }else{
                File file = new File(paths[0]);
                InputStream inputStream = new FileInputStream(file);
                client.files().uploadBuilder(destination + "/" + file.getName()).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean upload(String destination, String zipName, String... paths) {
        try {
            if(destination.equals("/")){
                destination = "";
            }
            zipFiles(paths);
            String originName = paths[0].substring(0,paths[0].lastIndexOf(".")) + ".zip";
            File origin = new File(originName);
            InputStream inputStream = new FileInputStream(origin);
            client.files().uploadBuilder(destination + "/" + zipName + ".zip").withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            inputStream.close();
            origin.delete();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong...");
        }
        return true;
    }

    public boolean download(String path) {
        try {
            String home = System.getProperty("user.home");
            String fileName;
            if(path.charAt(0) != '/')
                path = "/" + path;
            fileName = path.substring(path.lastIndexOf("/")+1);
            File file = new File(home + File.separator + fileName);
            OutputStream outputStream = new FileOutputStream(file);
            client.files().download(path).download(outputStream);
            outputStream.close();
            System.out.println("File " + fileName + " downloaded at this location: " + file.getPath());
        } catch (Exception e) {
            System.out.println("Something went wrong...");
        }
        return false;
    }

    public void addMeta(String path, String key, String value) {

    }

    public void getMeta(String path, String key) {

    }

    public void addUser(String name, String password, UserPrivilege privilege) {

    }

    public boolean mkDir(String[] arguments) {
        for (String dirName : arguments) {
            List<String> strings = new ArrayList<String>();
            if (dirName.contains("{") && dirName.contains("}")) {
                String name = dirName.substring(0, dirName.indexOf("{"));
                String pom = dirName.substring(dirName.indexOf("{") + 1, dirName.indexOf("}"));
                String[] fromTo = pom.split("-");
                int from = Integer.parseInt(fromTo[0]);
                int to = Integer.parseInt(fromTo[1]);
                for (int i = from; i <= to; i++) {
                    strings.add(name + i);
                }
                try {
                    client.files().createFolderBatchBuilder(strings).start();
                } catch (DbxException e) {
                    System.out.println("There was an error.");
                    return false;
                } catch (IllegalArgumentException e2) {
                    System.out.println("You are probably missing /");
                    return false;
                }
            } else {
                try {
                    CreateFolderResult folder = client.files().createFolderV2(dirName, true);
                    if (folder.getMetadata().getPathLower().equals(path + dirName)) {
                        System.out.println("Done!");
                    }
                } catch (DbxException e) {
                    System.out.println("Something went wrong.");
                    return false;
                } catch (IllegalArgumentException e2) {
                    System.out.println("You are probably missing /");
                    return false;
                }
            }
        }
        return true;
    }

    public void mkFile(String path) {
    }

    public void deleteItem(String path) {
        try {
            DeleteResult deleteResult = client.files().deleteV2(path);
            if (path.equals(deleteResult.getMetadata().getPathLower())) {
                System.out.println("Done!");
            }
            System.out.println(deleteResult.getMetadata().getPathLower());
        } catch (DbxException e) {
            System.out.println("Something went wrong.");
        }
    }


    public boolean isLoggedIn() {
        if (currentUser != null) return true;
        return false;
    }

    public boolean isAdmin() {
        //  if (currentPrivilege.equals(UserPrivilege.ADMIN)) return true;
        return true;
    }

    public boolean isBlacklisted(String extension) {
        return false;
    }

    public void addBlacklisted(String extension) {

    }

    public void removeBlacklisted(String extension) {

    }

    public void Search(String fileName) {
        Long max = 100L;
        Long stat = 0L;
        SearchResult sresult;
        SearchMode mode = SearchMode.FILENAME;
        try {
            sresult = client.files().searchBuilder("", fileName).withMode(mode).withMaxResults(max).withStart(stat).start();
            List<SearchMatch> res = sresult.getMatches();
            for (int i = 0; i < res.size(); i++) {
                System.out.println(res.get(i).getMetadata().getPathLower());
            }
        } catch (DbxException e) {
            System.out.println("Something went wrong ¯\\_(ツ)_/¯ pls try again");
        }
    }

    public void lsDir(String path, boolean subdirectories, boolean isDir) {
        ListFolderResult result = null;
//        SearchBuilder srcb;
//        SearchMode mode = SearchMode.FILENAME;
//        try {
//            SearchResult results = client.files().
//            List<SearchMatch> matches = results.getMatches();
//            for (int i = 0; i < matches.size(); i++) {
//                System.out.println(matches.get(i).toString());
//            }
//        } catch (DbxException e) {
//            e.printStackTrace();
//        }
        if (path.equals(File.separator)) {
            path = "";
        }
        try {
            result = client.files().listFolder(path);
            while (true) {
                for (Metadata metadata : result.getEntries()) {
                    if (metadata instanceof FolderMetadata && subdirectories) {
                        listDir((FolderMetadata) metadata, isDir);
                    } else if (!isDir) {
                        System.out.println(metadata.getPathLower());
                    }
                }
                if (!result.getHasMore()) {
                    break;
                }
                result = client.files().listFolderContinue(result.getCursor());
            }
        } catch (DbxException e) {
            System.out.println("Something went wrong ¯\\_(ツ)_/¯ pls try again");
        }
    }

    private void listDir(FolderMetadata metadata, boolean onlyDir) {
        try {
            System.out.println(metadata.getPathLower());
            ListFolderResult result = client.files().listFolder(metadata.getPathLower());
            while (true) {
                for (Metadata md : result.getEntries()) {
                    if (md instanceof FolderMetadata) {
                        listDir((FolderMetadata) md, onlyDir);
                    }
                    if (!onlyDir && !(md instanceof FolderMetadata)) {
                        System.out.println(md.getPathLower());
                    }
                }
                if (!result.getHasMore()) {
                    break;
                }
            }
        } catch (DbxException e) {
            System.out.println("Something went wrong ¯\\_(ツ)_/¯ pls try again");
        }
    }

    public void help() {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("FUNCTION", "DESCRIPTION", "ARGUMENTS");
        at.addRule();
        at.addRow("upload", "uploads chosen files/file to chosen destination", "upload <destination path>;<name(optional if zip)>;<path1>,<path2>");
        at.addRule();
        at.addRow("download", "downloads files from chosen path to user.home", "download <path>");
        at.addRule();
        at.addRow("search", "searches specific file", "search <file Name>");
        at.addRule();
        at.addRow("addMeta", "adds meta data to a chosen file", "addMeta <path>;<key value>");
        at.addRule();
        at.addRow("getMeta", "grabs the meta data from a chosen file", "getMeta <path>;<key>");
        at.addRule();
        at.addRow("addUser", "adds user to the current storage", "addUser <username> <password> <userPrivileges(admin/guest)>");
        at.addRule();
        at.addRow("mkDir", "makes directory to the chosen path from storage root. If no directory is given, root is chosen", "mkDir <path> <dirName> |" +
                "mkDir <dirName> | mkDir <path> <dirName{1-5}> | mkDir <dirName{1-5}>");
        at.addRule();
        at.addRow("mkFile", "makes file to the chosen path", "mkFile <path + fileName>");
        at.addRule();
        at.addRow("deleteItem", "deletes item at a chosen path", "deleteItem <path> <fileName>");
        at.addRule();
        at.addRow("isLoggedIn", "checks if user is logged in", "No arguments needed");
        at.addRule();
        at.addRow("isAdmin", "checks if current user is admin", "No arguments needed");
        at.addRule();
        at.addRow("isBlacklisted", "checks if given extension is blacklisted in this storage", "isBlacklisted <extension(.exe)>");
        at.addRule();
        at.addRow("addBlacklisted", "adds extension to a blacklist in this storage", "addBlacklisted <extension(.exe)>");
        at.addRule();
        at.addRow("removeBlacklisted", "removes extension from a blacklist in this storage", "removeBlacklisted <extension(.exe)>");
        at.addRule();
        at.addRow("lsDir", "prints all files in given path (option for subdirectories)", "lsDir <path> <subdirectories(true/false)> <only directories(true/false)>");
        at.addRule();
        at.addRow("disconnect", "disconnects user from storage", "No arguments needed");
        at.addRule();
        at.addRow("exit", "terminates program", "No arguments needed");
        at.addRule();
        String rend = at.render(150);
        System.out.println(rend);
    }

    private String returnExtension(File file) {
        String extension = "";
        int i = file.getName().lastIndexOf('.');
        int p = Math.max(file.getName().lastIndexOf('/'), file.getName().lastIndexOf('\\'));
        if (i > p) {
            extension = ".";
            extension += file.getName().substring(i + 1);
        }
        return extension;
    }

    private void zipFiles(String... filePaths) {
        try {
            List<String> srcFiles = Arrays.asList(filePaths);
            List<String> srcFiles2 = new ArrayList<String>();
            for (int i = 0; i < srcFiles.size(); i++) {
                if (!isBlacklisted(returnExtension(new File(srcFiles.get(i))))) {
                    srcFiles2.add(srcFiles.get(i));
                }
            }
            String zipName = filePaths[0].substring(0,filePaths[0].lastIndexOf(".")) + ".zip";
            FileOutputStream fos = new FileOutputStream(zipName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for (String srcFile : srcFiles2) {
                File fileToZip = new File(srcFile);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
            zipOut.close();
            fos.close();
        } catch (Exception e) {

        }
    }

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
