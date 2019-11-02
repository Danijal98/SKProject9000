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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ConnectionImplementation implements Connection {

    private String path;
    private String currentUser;
    private UserPrivilege currentPrivilege;

    ConnectionImplementation(String path, String user, String password) {
        this.path = path;
        File file = new File(this.path + File.separator + "users.json");
        if (!file.exists()) {
            addUser(user, password, UserPrivilege.ADMIN);
            file = new File(this.path + File.separator + "users.json");
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

    public boolean upload(String... paths) {
        return false;
    }

    public boolean download(String path) {
        if (!path.contains(this.path)) {
            System.out.println("Can't download from there.");
            return false;
        }
        String home = System.getProperty("user.home");
        File source = new File(path);
        File dest = new File(home + File.separator + "Downloads" + File.separator + source.getName());
        if (downloadDir(source, dest)) {
            System.out.println("Done!");
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
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void set_meta(String path, String key, String value) {

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
                    jsonArray.put(jsonObject);
                    fileReader.close();
                    FileWriter fileWriter = new FileWriter(file, false);
                    fileWriter.write(jsonObjectExisting.toString());
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
                e.printStackTrace();
            }
        }
        System.out.println("Done!");
    }

    public boolean mkDir(String[] arguments) {
        for (String p : arguments) {
            String[] parts = p.split(" ");
            String path = "";
            String dirName = "";
            if(parts.length>1){
                path = parts[0];
                dirName = parts[1];
            }else{
                dirName = parts[0];
            }
            if (dirName.contains("{") && dirName.contains("}")) {
                String name = dirName.substring(0,dirName.indexOf("{"));
                String pom = dirName.substring(dirName.indexOf("{")+1,dirName.indexOf("}"));
                String[] fromTo = pom.split("-");
                int from = Integer.parseInt(fromTo[0]);
                int to = Integer.parseInt(fromTo[1]);
                for (int i = from;i<=to;i++){
                    File file = new File(this.path + path + File.separator + name + i);
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
                File file = new File(this.path + path + File.separator + dirName);
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
        //TODO prosledjeni path bi trebalo da se nalepi na this.path, korisnik ne sme da pravi van skladista
        if (!path.contains(this.path)) {
            System.out.println("Can't do that!");
            return;
        }
        File file = new File(path);
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
            e.printStackTrace();
            System.out.println("Something went in and out and really bad.");
        }
    }

    public void deleteItem(String path) {
        File file = new File(path);
        if (!path.contains(this.path) || currentPrivilege.equals(UserPrivilege.GUEST) || file.getName().equals("users.json") || file.getName().equals("blacklisted.json") || currentPrivilege.equals(UserPrivilege.GUEST)) {
            System.out.println("Can't do that");
            return;
        }
        if (deleteFolder(path)) {
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
                e.printStackTrace();
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
                    System.out.println(extension + " is blacklisted.");
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(extension + " is not blacklisted.");
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
                e.printStackTrace();
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
                e.printStackTrace();
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
                jsonArray.put(extension);
                fileReader.close();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String ex = jsonArray.get(i).toString();
                    if (ex.equals(extension)) {
                        jsonArray.remove(i);
                        return;
                    }
                }
                FileWriter fileWriter = new FileWriter(file, false);
                fileWriter.write(jsonObject.toString());
                fileWriter.close();
                System.out.println("Done!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void lsDir(String path, boolean subdirectories) { //da se doda da prikaze i za poddirektorijum
        if (path.equals("")) {
            path = this.path;
        }
        List<File> files = new ArrayList<File>();
        getFiles(path, files, subdirectories);
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).toString().equals("users.json") || files.get(i).toString().equals("blacklisted.json"))
                continue;
            System.out.println(files.get(i).getName());
        }
    }

    public void help() {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("FUNCTION", "DESCRIPTION", "ARGUMENTS");
        at.addRule();
        at.addRow("upload", "upload - ", "");
        at.addRule();
        at.addRow("download", "download - ", "");
        at.addRule();
        at.addRow("set_meta", "sets meta data to a chosen file", "set_meta <path> <key value>");
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
        at.addRow("isBlacklisted", "checks if given extension is blacklisted in this storage", "isBlacklisted <extension>");
        at.addRule();
        at.addRow("addBlacklisted", "adds extension to a blacklist in this storage", "addBlacklisted <extension>");
        at.addRule();
        at.addRow("removeBlacklisted", "removes extension from a blacklist in this storage", "removeBlacklisted <extension>");
        at.addRule();
        at.addRow("lsDir", "prints all files in given path (option for subdirectories)", "lsDir <path> <subdirectories(true/false)>");
        at.addRule();
        String rend = at.render(200);
        System.out.println(rend);
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
            File firstFile = new File(filePaths[0]);
            String zipFileName = firstFile.getName().concat(".zip");

            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (String aFile : filePaths) {
                zos.putNextEntry(new ZipEntry(new File(aFile).getName()));

                byte[] bytes = Files.readAllBytes(Paths.get(aFile));
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            }

            zos.close();

        } catch (FileNotFoundException ex) {
            System.err.println("A file does not exist: " + ex);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }
    }

}
