package remote.implementation;

import api.documentation.Connection;
import api.documentation.UserPrivilege;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import de.vandermeer.asciitable.AsciiTable;

import java.io.File;
import java.util.List;

public class ConnectionImplementation implements Connection {

    private String path;
    private String currentUser;
    private String password;
    private DbxClientV2 client;

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
        return false;
    }

    public boolean upload(String destination, String zipName, String... paths) {
        return false;
    }

    public boolean download(String path) {
        return false;
    }

    public void addMeta(String path, String key, String value) {

    }

    public void getMeta(String path, String key) {

    }

    public void addUser(String name, String password, UserPrivilege privilege) {

    }

    public boolean mkDir(String[] paths) {
        return true;
    }

    public void mkFile(String path) {

    }

    public void deleteItem(String path) {

    }

    public boolean isLoggedIn() {
        return true;
    }

    public boolean isAdmin() {
        return false;
    }

    public boolean isBlacklisted(String extension) {
        return false;
    }

    public void addBlacklisted(String extension) {

    }

    public void removeBlacklisted(String extension) {

    }

    public void Search(String fileName) {

    }

    public void lsDir(String path, boolean subdirectories, boolean isDir) {
        /*
        try {

            JSONObject jsonObject = new JSONObject(client.files().listFolder(""));
            JSONArray jsonArray = jsonObject.getJSONArray("entries");
            for(int i=0;i<jsonArray.length();i++){
                JSONObject file = jsonArray.getJSONObject(i);
                System.out.println(file.getString("name"));
            }
        } catch (DbxException e) {
            System.out.println("Something went wrong ¯\\_(ツ)_/¯ pls try again");
        }
        */
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

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
