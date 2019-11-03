package local.implemetation;

import api.documentation.Connection;
import api.documentation.UserPrivilege;
import de.vandermeer.asciitable.AsciiTable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ConnectionImplementation implements Connection {

    private final String STORAGE = "storage";
    private final String META_STORAGE = "metaStorage";
    private String path;
    private String currentUser;
    private UserPrivilege currentPrivilege;

    ConnectionImplementation(String path, String user, String password) {
        this.path = path;
        File dir = new File(this.path);
        File file = new File(this.path + File.separator + "users.json");
        if (!dir.exists()) {
            String str = "";
            mkDir(str.split(""));
            addUser(user, password, UserPrivilege.ADMIN);
            file = new File(this.path + File.separator + "users.json");
            File storage = new File(this.path + File.separator + STORAGE);
            File metaStorage = new File(this.path + File.separator + META_STORAGE);
            storage.mkdirs();
            metaStorage.mkdirs();
        }
        try {
            FileReader fileReader = new FileReader(file);
            JSONObject jsonObject = new JSONObject(new JSONTokener(fileReader));
            JSONArray jsonArray = jsonObject.getJSONArray("users");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonobj1 = jsonArray.getJSONObject(i);
                if (jsonobj1.get("username").toString().equals(user) && jsonobj1.get("password").toString().equals(password)) {
                    currentUser = user;
                    currentPrivilege = getEnumPrivilege(jsonobj1.get("privilege").toString());
                }
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (currentUser == null && currentPrivilege == null) {

        }
    }

    private UserPrivilege getEnumPrivilege(String privilege) {
        if (privilege.equals("ADMIN"))
            return UserPrivilege.ADMIN;
        return UserPrivilege.GUEST;
    }

    @Override
    public boolean upload(String destination, String[] paths) {
        if (paths.length > 1) {
            File pom = new File(paths[0]);
            String createdName = pom.getName();
            return upload(destination, createdName, paths);
        } else {
            File src = new File(paths[0]);
            File dest = new File(this.path + File.separator + STORAGE + File.separator + destination + File.separator + src.getName());
            downloadDir(src, dest);
            return true;
        }
    }

    @Override
    public boolean upload(String destination, String zipName, String[] paths) {
        //Zipujemo sve, prebacimo taj zip u destination, obrisemo zip originalni
        zipFiles(paths);
        File origin = new File(paths[0].concat(".zip"));
        File copy = new File(this.path + File.separator + STORAGE + File.separator + destination + File.separator + zipName + ".zip");
        downloadDir(origin, copy);
        origin.delete();
        return true;
    }

    public boolean download(String path) {
        String home = System.getProperty("user.home");
        File source = new File(this.path + File.separator + STORAGE + File.separator + path);
        File dest = new File(home + File.separator + source.getName());
        if (downloadDir(source, dest)) {
            System.out.println("File downloaded at " + dest.getPath());
            return true;
        }
        return false;
    }

    private boolean downloadDir(File source, File dest) {
        if (source.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String[] children = source.list();
            for (int i = 0; i < children.length; i++) {
                downloadDir(new File(source, children[i]),
                        new File(dest, children[i]));
            }
        } else {
            OutputStream out = null;
            InputStream in = null;
            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(dest);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                System.out.println("File not found!");
                return false;
            } catch (IOException e) {
                System.out.println("Something went wrong: IO Exception");
                return false;
            }
        }
        return true;
    }

    public void addMeta(String path, String key, String value) {
        String newPath = META_STORAGE + File.separator + path;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(key, value);
        File originalFile = new File(this.path + File.separator + STORAGE + File.separator + path);
        if (!originalFile.exists()) {
            System.out.println("File doesn't exist.");
            return;
        }
        File file = new File(this.path + File.separator + newPath + ".json");
        file.getParentFile().mkdirs();

        if (file.exists()) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
                JSONObject jsonObjectExisting = new JSONObject(new JSONTokener(fileReader));
                JSONArray array = jsonObjectExisting.getJSONArray("meta");
                array.put(jsonObject);
                fileReader.close();
                FileWriter fileWriter = new FileWriter(file, false);
                fileWriter.write(jsonObjectExisting.toString());
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            FileWriter fileWriter = null;
            try {
                JSONObject jsonobj = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject);
                jsonobj.put("meta", jsonArray);
                fileWriter = new FileWriter(file);
                fileWriter.write(jsonobj.toString());
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getMeta(String path, String key) {
        String newPath = META_STORAGE + File.separator + path;

        File file = new File(this.path + File.separator + newPath + ".json");
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            JSONObject jsonObjectExisting = new JSONObject(new JSONTokener(fileReader));
            JSONArray array = jsonObjectExisting.getJSONArray("meta");
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                if (jsonObject.has(key)) {
                    System.out.println(jsonObject.getString(key));
                }
            }
            fileReader.close();
        } catch (IOException e) {

        }

    }

    public void addUser(String name, String password, UserPrivilege privilege) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", name);
        jsonObject.put("password", password);
        File file = new File(path + File.separator + "users.json");
        if (file.exists()) {
            if (currentPrivilege.equals(UserPrivilege.ADMIN)) {
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
                            System.out.println("User already exist");
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
                }
            } else {
                System.out.println("Sorry, you can't do that.");
                return;
            }
        } else {
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
            }
        }
        System.out.println("User added!");
    }

    public boolean mkDir(String[] arguments) {
        for (String p : arguments) {
            String[] parts = p.split(" ");
            String path = "";
            String dirName = "";
            if (parts.length > 1) {
                path = parts[0];
                dirName = parts[1];
            } else {
                dirName = parts[0];
            }
            if (dirName.contains("{") && dirName.contains("}")) {
                String name = dirName.substring(0, dirName.indexOf("{"));
                String pom = dirName.substring(dirName.indexOf("{") + 1, dirName.indexOf("}"));
                String[] fromTo = pom.split("-");
                int from = Integer.parseInt(fromTo[0]);
                int to = Integer.parseInt(fromTo[1]);
                for (int i = from; i <= to; i++) {
                    File file = new File(this.path + File.separator + STORAGE + File.separator + path + File.separator + name + i);
                    if (!file.exists()) {
                        if (file.mkdirs()) {
                            System.out.println("Directory created!");
                        } else {
                            System.out.println("Error!");
                        }
                    } else {
                        System.out.println("Directory at this location already exist: " + path);
                    }
                }
            } else {
                File file = new File(this.path + File.separator + STORAGE + File.separator + path + File.separator + dirName);
                if (!file.exists()) {
                    if (file.mkdirs()) {
                        System.out.println("Directory created!");
                    } else {
                        System.out.println("Error!");
                    }
                } else {
                    System.out.println("Directory at this location already exist: " + path);
                }
            }
        }
        return true;
    }

    public void mkFile(String path) {
        File file = new File(this.path + File.separator + STORAGE + File.separator + path);
        String extension = "";

        int i = file.getName().lastIndexOf('.');
        int p = Math.max(file.getName().lastIndexOf('/'), file.getName().lastIndexOf('\\'));

        if (i > p) {
            extension = ".";
            extension += file.getName().substring(i + 1);
        }
        if (isBlacklisted(extension)) {
            System.out.println("Extension is blacklisted.");
            return;
        }
        if (file.exists()) {
            System.out.println("File already exists!");
            return;
        }
        try {
            if (file.createNewFile()) {
                System.out.println("File is created.");
            } else {
                System.out.println("Something went wrong...");
            }
        } catch (IOException e) {
            System.out.println("Something went in and out and really bad.");
        }
    }

    public void deleteItem(String path) {
        File file = new File(path);
        if (currentPrivilege.equals(UserPrivilege.GUEST) || file.getName().equals("users.json") || file.getName().equals("blacklisted.json") || currentPrivilege.equals(UserPrivilege.GUEST)) {
            System.out.println("Can't do that");
            return;
        }
        if (deleteFolder(this.path + File.separator + STORAGE + File.separator + path)) {
            System.out.println("Done!");
        } else {
            System.out.println("Something went wrong.");
        }
    }

    private static void deleteFile(File file) throws IOException {
        if (file.isDirectory()) {
            String[] files = file.list();
            for (String f : files) {
                File fileDelete = new File(file, f);
                deleteFile(fileDelete);
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    public static boolean deleteFolder(String dir) {
        File directory = new File(dir);
        if (!directory.exists()) {
            System.out.println("File does not exist " + directory);
            return false;
        } else {
            try {
                deleteFile(directory);
                directory.delete();
            } catch (IOException e) {
                System.out.println("Something went wrong: IO Exception");
                return false;
            }
        }
        return true;
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
        File file = new File(this.path + File.separator + "blacklisted.json");
        if (!file.exists()) {
            return false;
        }
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            JSONObject jsonObject = new JSONObject(new JSONTokener(fileReader));
            JSONArray jsonArray = jsonObject.getJSONArray("blacklisted");
            for (int i = 0; i < jsonArray.length(); i++) {
                String ex = jsonArray.get(i).toString();
                if (ex.equals(extension)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        }
        return false;
    }

    public void addBlacklisted(String extension) {
        File file = new File(this.path + File.separator + "blacklisted.json");
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                JSONObject jsonObject = new JSONObject(new JSONTokener(fileReader));
                JSONArray jsonArray = jsonObject.getJSONArray("blacklisted");
                jsonArray.put(extension);
                fileReader.close();
                FileWriter fileWriter = new FileWriter(file, false);
                fileWriter.write(jsonObject.toString());
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Something went wrong: IO Exception");
            }
        } else {
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(file, false);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("blacklisted", new JSONArray().put(extension));
                fileWriter.write(jsonObject.toString());
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Something went wrong: IO Exception");
            }
        }
        System.out.println("Done!");
    }

    public void removeBlacklisted(String extension) {
        File file = new File(this.path + File.separator + "blacklisted.json");
        if (file.exists()) {
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
                System.out.println("Done!");
            } catch (IOException e) {
                System.out.println("Something went wrong: IO Exception");
            }
        } else {
            System.out.println("There are no blacklisted.");
        }
    }

    public void lsDir(String path, boolean subdirectories, boolean onlyDirs) {
        List<File> files = new ArrayList<File>();
        getFiles(this.path + File.separator + STORAGE + File.separator + path, files, subdirectories);
        for (int i = 0; i < files.size(); i++) {
            if (onlyDirs) {
                if (files.get(i).isDirectory()) {
                    System.out.println(files.get(i).getPath().replace(this.path + File.separator + STORAGE + File.separator, ""));
                }
                continue;
            }
            System.out.println(files.get(i).getPath().replace(this.path + File.separator + STORAGE + File.separator, ""));
        }
    }

    public void help() {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("FUNCTION", "DESCRIPTION", "ARGUMENTS");
        at.addRule();
        at.addRow("upload", "uploads chosen files/file to chosen destination", "upload <path>;<name(optional if zip)>;<path1>,<path2>");
        at.addRule();
        at.addRow("download", "downloads files from chosen path to user.home", "download <path>");
        at.addRule();
        at.addRow("addMeta", "adds meta data to a chosen file", "addMeta <path>;<key value>");
        at.addRule();
        at.addRow("addUser", "adds user to the current storage", "addUser <username> <password> <userPrivileges(admin/guest)>");
        at.addRule();
        at.addRow("mkDir", "makes directory to the chosen path from storage root. If no directory is given, root is chosen", "mkDir <path> <dirName> |" +
                "mkDir <dirName> | mkDir <path> <dirName{1-5}> | mkDir <dirName{1-5}>");
        at.addRule();
        at.addRow("mkFile", "makes file to the chosen path", "mkFile <path> <fileName>");
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
        String rend = at.render(200);
        System.out.println(rend);
    }

    public void clearScreen() {
        for (int i = 0; i < 50; i++)
            System.out.println();
    }

    private void getFiles(String directoryName, List<File> files, boolean subdirectories) {
        File directory = new File(directoryName);
        File[] fileList = directory.listFiles();
        if (fileList != null)
            for (File file : fileList) {
                files.add(file);
                if (file.isDirectory() && subdirectories) {
                    getFiles(file.getAbsolutePath(), files, true);
                }
            }
    }

    private void zipFiles(String... filePaths) {
        try {
            List<String> srcFiles = Arrays.asList(filePaths);
            FileOutputStream fos = new FileOutputStream(filePaths[0].concat(".zip"));
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for (String srcFile : srcFiles) {
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

    public void Search(String fileName) {
        List<File> files = new ArrayList<File>();
        getFiles(this.path + File.separator + STORAGE, files, true);
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).getName().equals(fileName))
                System.out.println(files.get(i).getPath().replace(this.path + File.separator + STORAGE + File.separator, ""));
        }
    }

}
