package api.documentation;

public interface Connection {

    boolean upload(String destination, String... paths); //one or more and ZIP

    boolean upload(String destination, String zipName, String... paths);

    boolean download(String path);

    void addMeta(String path, String key, String value);

    void getMeta(String path, String key);

    void addUser(String name, String password, UserPrivilege privilege);

    boolean mkDir(String[] paths);

    void mkFile(String path);

    void deleteItem(String path);

    boolean isLoggedIn();

    boolean isAdmin();

    boolean isBlacklisted(String extension);

    void addBlacklisted(String extension);

    void removeBlacklisted(String extension);

    void lsDir(String path, boolean subdirectories, boolean isDir);

    void clearScreen();

    void help();

    void Search(String fileName);
}
