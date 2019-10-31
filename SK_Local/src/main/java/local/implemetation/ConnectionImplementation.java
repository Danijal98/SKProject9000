package local.implemetation;

import api.documentation.Connection;
import api.documentation.UserPrivilege;

public class ConnectionImplementation implements Connection {

    public boolean upload(String... paths) {
        return false;
    }

    public boolean download(String... paths) {
        return false;
    }

    public void set_meta(String path, String key, String value) {

    }

    public void addUser(String name, String password, UserPrivilege privilege) {

    }

    public void mkDir(String path, String dirName) {
        // Tree
        // /root/folder1/folder2/fajl
        // /root/folder1/fajl
    }

    public void mkFile(String path, String fileName) {

    }

    public void deleteItem(String path, String fileName) {

    }

    public boolean isLoggedIn() {
        return false;
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

    public void lsDir(String path) {

    }
}
