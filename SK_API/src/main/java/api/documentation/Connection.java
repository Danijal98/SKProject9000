package api.documentation;

public interface Connection {

    boolean upload(String... paths); //one or more and ZIP

    boolean download(String path);

    void set_meta(String path, String key, String value);

    void addUser(String name, String password, UserPrivilege privilege);

    boolean mkDir(String[] paths);

    void mkFile(String path);

    void deleteItem(String path);

    boolean isLoggedIn();

    boolean isAdmin();

    boolean isBlacklisted(String extension);

    void addBlacklisted(String extension);

    void removeBlacklisted(String extension);

    void lsDir(String path, boolean subdirectories);

    void clearScreen();
    //Change privileges (Username, privilege type)

    //Display help
    void help();

}
