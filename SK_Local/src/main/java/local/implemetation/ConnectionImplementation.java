package local.implemetation;

import api.documentation.Connection;
import api.documentation.UserPrivilege;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    public void mkDir(String path, String dirName) {
        // Tree
        // /root/folder1/folder2/fajl
        // /root/folder1/fajl
    }

    public void mkFile(String path) {
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
}
