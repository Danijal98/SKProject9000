package remote.implementation;

import api.documentation.Connection;
import api.documentation.UserPrivilege;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import de.vandermeer.asciitable.AsciiTable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ConnectionImplementation implements Connection {

    private final String STORAGE = "/storage";
    private final String META_STORAGE = "/metaStorage";
    private String path;
    private String currentUser;
    private DbxClientV2 client;
    private UserPrivilege currentPrivilege;


    public ConnectionImplementation(String path, String currentUser, String password) {
        this.path = path;
        this.currentUser = currentUser;
    }

    public ConnectionImplementation(String path, String currentUser, String password, DbxClientV2 client) {
        this.path = path;
        this.currentUser = currentUser;
        this.client = client;
        this.currentPrivilege = UserPrivilege.ADMIN;
        try {
            if (!findIfFileExists(STORAGE)) {
                CreateFolderResult folder = client.files().createFolderV2(STORAGE);
            }
            if (!findIfFileExists(META_STORAGE)) {
                CreateFolderResult folder = client.files().createFolderV2(META_STORAGE);
            }
        } catch (DbxException e) {
            System.out.println("Something went wrong with the storage");
        } catch (IllegalArgumentException e2) {
            System.out.println("Something went wrong with the storage");
        }
        if (!findIfFileExists("/users.json")) {
            addUser(this.currentUser, password, this.currentPrivilege);
        }
    }

    public boolean upload(String destination, String... paths){
        if (!isAdmin()){
            System.out.println("This command is only for admin!");
            return true;
        }
        try {
            if (destination.equals("/")) {
                destination = "";
            }
            if (paths.length > 1) {
                File pom = new File(paths[0]);
                String createdName = pom.getName().substring(0, pom.getName().lastIndexOf("."));
                return upload(destination, createdName, paths);
            } else {
                File file = new File(paths[0]);
                if(isBlacklisted(returnExtension(file))){
                    System.out.println("You can't upload blacklisted extensions!");
                }
                InputStream inputStream = new FileInputStream(file);
                client.files().uploadBuilder(STORAGE + destination + "/" + file.getName()).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
                inputStream.close();
            }
        } catch (Exception e) {
            System.out.println("Something went wrong...");
        }
        return true;
    }

    public boolean upload(String destination, String zipName, String... paths) {
        if (!isAdmin()){
            System.out.println("This command is only for admin!");
            return true;
        }
        try {
            if (destination.equals("/")) {
                destination = "";
            }
            zipFiles(paths);
            String originName = paths[0].substring(0, paths[0].lastIndexOf(".")) + ".zip";
            File origin = new File(originName);
            InputStream inputStream = new FileInputStream(origin);
            client.files().uploadBuilder(STORAGE + destination + "/" + zipName + ".zip").withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            inputStream.close();
            origin.delete();
        } catch (Exception e) {
            System.out.println("Something went wrong...");
        }
        return true;
    }

    public boolean download(String path) {
        try {
            String home = System.getProperty("user.home");
            String fileName;
            if (path.charAt(0) != '/')
                path = "/" + path;
            fileName = path.substring(path.lastIndexOf("/") + 1);
            File file = new File(home + File.separator + fileName);
            OutputStream outputStream = new FileOutputStream(file);
            client.files().download(STORAGE + path).download(outputStream);
            outputStream.close();
            System.out.println("File " + fileName + " downloaded at this location: " + file.getPath());
            return true;
        } catch (Exception e) {
            System.out.println("Something went wrong...");
            return false;
        }
    }

    private boolean download(String path, boolean showPath) {
        try {
            String home = System.getProperty("user.home");
            String fileName;
            if (path.charAt(0) != '/')
                path = "/" + path;
            fileName = path.substring(path.lastIndexOf("/") + 1);
            File file = new File(home + File.separator + fileName);
            OutputStream outputStream = new FileOutputStream(file);
            client.files().download(STORAGE + path).download(outputStream);
            outputStream.close();
            if (showPath)
                System.out.println("File " + fileName + " downloaded at this location: " + file.getPath());
            return true;
        } catch (Exception e) {
            System.out.println("Something went wrong...");
            return false;
        }
    }

    public void addMeta(String path, String key, String value) {
        if (!isAdmin()){
            System.out.println("This command is only for admin!");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(key, value);
        String pom = path.substring(path.lastIndexOf("/") + 1);
        String fileName = pom.substring(0, pom.lastIndexOf(".")) + ".json";
        String home = System.getProperty("user.home");
        String downloadedPath = home + File.separator + fileName;
        File metaFile = new File(downloadedPath);

        if (findIfFileExists(path)) {
            String metaPath = path.replace(STORAGE, META_STORAGE);
            metaPath = metaPath.substring(0, metaPath.lastIndexOf(".")) + ".json";
            System.out.println(metaPath);
            if (findIfFileExists(metaPath)) {
                download(metaPath, false);
                FileReader fileReader;
                try {
                    fileReader = new FileReader(downloadedPath);
                    JSONObject jsonObjectExisting = new JSONObject(new JSONTokener(fileReader));
                    JSONArray array = jsonObjectExisting.getJSONArray("meta");
                    array.put(jsonObject);
                    fileReader.close();
                    FileWriter fileWriter = new FileWriter(metaFile, false);
                    fileWriter.write(jsonObjectExisting.toString());
                    fileWriter.close();
                    metaPath = metaPath.substring(0, metaPath.lastIndexOf("/"));
                    upload(metaPath, new String[]{metaFile.getPath()});
                    metaFile.delete();
                } catch (IOException e) {
                    System.out.println("Something went wrong: IO Exception");
                }
            } else {
                FileWriter fileWriter;
                try {
                    metaFile.createNewFile();
                    JSONObject mainJsn = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    mainJsn.put("meta", jsonArray);
                    fileWriter = new FileWriter(metaFile);
                    fileWriter.write(mainJsn.toString());
                    fileWriter.close();
                    metaPath = metaPath.substring(0, metaPath.lastIndexOf("/"));
                    upload(metaPath, new String[]{metaFile.getPath()});
                    metaFile.delete();
                } catch (IOException e) {
                    System.out.println("Something went wrong: IO Exception");
                }
            }
        } else {
            System.out.println("File not found");
        }
    }

    public void getMeta(String path, String key) {
        String pom = path.substring(path.lastIndexOf("/") + 1);
        String fileName = pom.substring(0, pom.lastIndexOf(".")) + ".json";
        String home = System.getProperty("user.home");
        String downloadedPath = home + File.separator + fileName;
        String metaPath = path.replace(STORAGE, META_STORAGE);
        metaPath = metaPath.substring(0, metaPath.lastIndexOf(".")) + ".json";
        if (download(metaPath, false)) {
            File metaFile = new File(downloadedPath);
            FileReader fileReader;
            try {
                fileReader = new FileReader(metaFile);
                JSONObject jsonObjectExisting = new JSONObject(new JSONTokener(fileReader));
                JSONArray array = jsonObjectExisting.getJSONArray("meta");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    if (jsonObject.has(key)) {
                        System.out.println(jsonObject.getString(key));
                        break;
                    }
                }
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Something went wrong...");
            }
            metaFile.delete();
        } else {
            System.out.println("Something went very wrong, hmm...");
        }
    }

    public void addUser(String name, String password, UserPrivilege privilege) {
        if (!isAdmin()){
            System.out.println("This command is only for admin!");
            return;
        }
        if (!findIfFileExists("/users.json")) {
            addFirstUser(name, password, privilege);
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", name);
        jsonObject.put("password", password);
        download("/users.json", false);
        String home = System.getProperty("user.home");
        File file = new File(home + File.separator + "users.json");
        if (file.exists()) {
            jsonObject.put("privilege", privilege);
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
                JSONObject jsonObjectExisting = new JSONObject(new JSONTokener(fileReader));
                JSONArray jsonArray = jsonObjectExisting.getJSONArray("users");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsobj = jsonArray.getJSONObject(i);
                    if (jsobj.getString("username").equals(name)) {
                        fileReader.close();
                        System.out.println("User already exists");
                        file.delete();
                        return;
                    }
                }
                jsonArray.put(jsonObject);
                fileReader.close();
                FileWriter fileWriter = new FileWriter(file, false);
                fileWriter.write(jsonObjectExisting.toString());
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Something went wrong: IO Exception.");
                file.delete();
                return;
            }
        } else System.out.println("File doesn't exist?");
        upload("", new String[]{file.getAbsolutePath()});
        System.out.println("User added!");
        file.delete();
    }

    private void addFirstUser(String name, String password, UserPrivilege privilege) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", name);
        jsonObject.put("password", password);
        String home = System.getProperty("user.home");
        File file = new File(home + File.separator + "users.json");
        jsonObject.put("privilege", UserPrivilege.ADMIN);
        FileWriter fileWriter = null;
        try {
            JSONObject jsonobj = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject);
            jsonobj.put("users", jsonArray);
            fileWriter = new FileWriter(file);
            fileWriter.write(jsonobj.toString());
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Something went wrong: IO Exception");
            file.delete();
            return;
        }
        upload("", new String[]{file.getAbsolutePath()});
        System.out.println("User added!");
        file.delete();
    }

    public boolean mkDir(String[] arguments) {
        if (!isAdmin()){
            System.out.println("This command is only for admin!");
            return true;
        }
        for (String dirName : arguments) {
            if (!dirName.startsWith("/")) {
                System.out.println("Check path.(Start with /)");
                return false;
            }
            List<String> strings = new ArrayList<String>();
            if (dirName.contains("{") && dirName.contains("}")) {
                String name = dirName.substring(0, dirName.indexOf("{"));
                String pom = dirName.substring(dirName.indexOf("{") + 1, dirName.indexOf("}"));
                String[] fromTo = pom.split("-");
                int from = Integer.parseInt(fromTo[0]);
                int to = Integer.parseInt(fromTo[1]);
                for (int i = from; i <= to; i++) {
                    if (!findIfFileExists(STORAGE + name + i)) {
                        strings.add(STORAGE + name + i);
                    } else {
                        System.out.println("This file already exists " + name + i);
                    }
                }
                try {
                    client.files().createFolderBatchBuilder(strings).start();
                } catch (DbxException e) {
                    System.out.println("There was an error.");
                    return false;
                } catch (IllegalArgumentException e2) {
                    System.out.println("You are probably missing / somewhere, check path!");
                    return false;
                }
            } else {
                try {
                    CreateFolderResult folder = client.files().createFolderV2(STORAGE + dirName, true);
                    if (folder.getMetadata().getPathDisplay().equals(STORAGE + path + dirName)) {
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
        if (!isAdmin()){
            System.out.println("This command is only for admin!");
            return;
        }
        String home = System.getProperty("user.home");
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        path = path.replace(path.substring(path.lastIndexOf("/")), "");
        File file = new File(home + File.separator + fileName);

        if(findIfFileExists(path)){
            System.out.println("File already exists!");
            return;
        }

        if (isBlacklisted(returnExtension(file))) {
            System.out.println("Extension is blacklisted.");
            return;
        }

        try {
            if (file.createNewFile()) {
                upload(path, new String[]{file.getPath()});
                file.delete();
                System.out.println("File is created.");
            } else {
                System.out.println("Something went wrong...");
            }
        } catch (IOException e) {
            System.out.println("Something went in and out and really bad.");
        }
    }

    public void deleteItem(String path) {
        if (!isAdmin()){
            System.out.println("This command is only for admin!");
            return;
        }
        if (!findIfFileExists(STORAGE + path)) {
            System.out.println("File doesn't exist.");
        }
        try {
            DeleteResult deleteResult = client.files().deleteV2(STORAGE + path);
            if (path.equals(deleteResult.getMetadata().getPathDisplay())) {
                System.out.println("Done!");
            }
        } catch (DbxException e) {
            System.out.println("Something went wrong.");
        }
    }


    public boolean isLoggedIn() {
        if (currentUser != null) return true;
        return false;
    }

    public boolean isAdmin() {
        if (currentPrivilege.equals(UserPrivilege.ADMIN)) return true;
        return false;
    }

    public boolean isBlacklisted(String extension) {
        if (!findIfFileExists("/blacklisted.json")) {
            return false;
        }
        download("/blacklisted.json", false);
        String home = System.getProperty("user.home");
        File file = new File(home + File.separator + "blacklisted.json");
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            JSONObject jsonObject = new JSONObject(new JSONTokener(fileReader));
            JSONArray jsonArray = jsonObject.getJSONArray("blacklisted");
            for (int i = 0; i < jsonArray.length(); i++) {
                String ex = jsonArray.get(i).toString();
                if (ex.equals(extension)) {
                    fileReader.close();
                    file.delete();
                    return true;
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        } catch (IOException e) {
            System.out.println("Something went wrong: IO Exception");
        }
        file.delete();
        return false;
    }

    public void addBlacklisted(String extension) {
        if (!isAdmin()){
            System.out.println("This command is only for admin!");
            return;
        }
        File file = null;
        if (findIfFileExists("/blacklisted.json")) {
            download("/blacklisted.json", false);
            String home = System.getProperty("user.home");
            file = new File(home + File.separator + "blacklisted.json");
            try {
                FileReader fileReader = new FileReader(file);
                JSONObject jsonObject = new JSONObject(new JSONTokener(fileReader));
                JSONArray jsonArray = jsonObject.getJSONArray("blacklisted");
                for (int i = 0; i < jsonArray.length(); i++) {
                    String string = jsonArray.getString(i);
                    if (string.equals(extension)) {
                        fileReader.close();
                        System.out.println("Blacklisted extension already exists");
                        file.delete();
                        return;
                    }
                }
                jsonArray.put(extension);
                fileReader.close();
                FileWriter fileWriter = new FileWriter(file, false);
                fileWriter.write(jsonObject.toString());
                fileWriter.close();
            } catch (IOException e) {
                file.delete();
                System.out.println("Something went wrong: IO Exception");
                return;
            }
        } else {
            String home = System.getProperty("user.home");
            file = new File(home + File.separator + "blacklisted.json");
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(file, false);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("blacklisted", new JSONArray().put(extension));
                fileWriter.write(jsonObject.toString());
                fileWriter.close();
            } catch (IOException e) {
                file.delete();
                System.out.println("Something went wrong: IO Exception");
                return;
            }
        }
        upload("", new String[]{file.getAbsolutePath()});
        file.delete();
        System.out.println("Done!");
    }

    public void removeBlacklisted(String extension) {
        if (!isAdmin()){
            System.out.println("This command is only for admin!");
            return;
        }
        if (findIfFileExists("/blacklisted.json")) {
            download("/blacklisted.json", false);
            String home = System.getProperty("user.home");
            File file = new File(home + File.separator + "blacklisted.json");
            try {
                FileReader fileReader = new FileReader(file);
                JSONObject jsonObject = new JSONObject(new JSONTokener(fileReader));
                JSONArray jsonArray = jsonObject.getJSONArray("blacklisted");
                for (int i = 0; i < jsonArray.length(); i++) {
                    String ex = jsonArray.get(i).toString();
                    if (ex.equals(extension)) {
                        jsonArray.remove(i);
                    }
                }
                fileReader.close();
                FileWriter fileWriter = new FileWriter(file, false);
                fileWriter.write(jsonObject.toString());
                fileWriter.close();
                upload("", new String[]{file.getAbsolutePath()});
                file.delete();
                System.out.println("Done!");
            } catch (IOException e) {
                System.out.println("Something went wrong: IO Exception");
                file.delete();
            }
        } else {
            System.out.println("There are no blacklisted.");
        }
    }

    public void Search(String fileName) {
        Long max = 100L;
        Long stat = 0L;
        SearchResult sresult;
        SearchMode mode = SearchMode.FILENAME;
        try {
            sresult = client.files().searchBuilder(STORAGE, fileName).withMode(mode).withMaxResults(max).withStart(stat).start();
            List<SearchMatch> res = sresult.getMatches();
            for (int i = 0; i < res.size(); i++) {
                System.out.println(res.get(i).getMetadata().getPathDisplay().replace(STORAGE, ""));
            }
        } catch (DbxException e) {
            System.out.println("Something went wrong ¯\\_(ツ)_/¯ pls try again");
        }
    }

    private boolean findIfFileExists(String path) {
        try {
            Metadata result = client.files().getMetadata(path);
            if (result.getPathDisplay().equals(path))
                return true;
        } catch (DbxException e) {
            return false;
        } catch (IllegalArgumentException e2) {
            return false;
        }
        return false;
    }

    public void lsDir(String path, boolean subdirectories, boolean isDir) {
        ListFolderResult result = null;
        try {
            result = client.files().listFolder(STORAGE + path);
            while (true) {
                for (Metadata metadata : result.getEntries()) {
                    if (metadata instanceof FolderMetadata && subdirectories) {
                        listDir((FolderMetadata) metadata, isDir);
                    } else if (!isDir) {
                        System.out.println(metadata.getPathDisplay().replace(STORAGE, ""));
                    } else if (isDir && !subdirectories && metadata instanceof FolderMetadata) {
                        System.out.println(metadata.getPathDisplay().replace(STORAGE, ""));
                    }
                }
                if (!result.getHasMore()) {
                    break;
                }
                result = client.files().listFolderContinue(result.getCursor());
            }
        } catch (DbxException e) {
            System.out.println("Something went wrong ¯\\_(ツ)_/¯ pls try again");
        } catch (IllegalArgumentException e2) {
            System.out.println("Check path.");
        }
    }

    private void listDir(FolderMetadata metadata, boolean onlyDir) {
        try {
            System.out.println(metadata.getPathDisplay().replace(STORAGE, ""));
            ListFolderResult result = client.files().listFolder(metadata.getPathDisplay());
            while (true) {
                for (Metadata md : result.getEntries()) {
                    if (md instanceof FolderMetadata) {
                        listDir((FolderMetadata) md, onlyDir);
                    }
                    if (!onlyDir && !(md instanceof FolderMetadata)) {
                        System.out.println(md.getPathDisplay().replace(STORAGE, ""));
                    }
                }
                if (!result.getHasMore()) {
                    break;
                }
            }
        } catch (DbxException e) {
            System.out.println("Something went wrong ¯\\_(ツ)_/¯ pls try again");
        } catch (IllegalArgumentException e2) {
            System.out.println("Check path.");
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
            String zipName = filePaths[0].substring(0, filePaths[0].lastIndexOf(".")) + ".zip";
            FileOutputStream fos = new FileOutputStream(zipName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for (String srcFile : srcFiles2) {
                File fileToZip = new File(srcFile);
                if(isBlacklisted(returnExtension(fileToZip))){
                    System.out.println("This extension is blacklisted. Skipping this file: " + fileToZip.getPath());
                    continue;
                }
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
