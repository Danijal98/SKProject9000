public interface Connection {

    boolean upload(String... paths);
    boolean download(String... paths);
    void set_meta(String path, String key, String value);
    void addUser(String name, String password);
    void mkDir(String path, String dirName);
    void mkFile(String path, String fileName);
    void deleteItem(String path, String fileName);
    boolean isLoggedIn();
    boolean isAdmin();

    boolean isBlacklisted(String extension);
    void addBlacklisted(String extension);
    void removeBlacklisted(String extension);
}
