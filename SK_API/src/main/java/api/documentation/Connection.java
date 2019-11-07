package api.documentation;

public interface Connection {

    /**
     * Uploads destination files to chosen path. Zips(default zip name) if more than one file
     * @param destination - where to upload
     * @param paths - file paths to upload
     * @return true - if successful
     */
    boolean upload(String destination, String... paths);

    /**
     * Uploads destination files to chosen path. Zips(given zip name) if more than one file
     * @param destination - where to upload
     * @param zipName - Name of zip
     * @param paths - file paths to upload
     * @return true - if successful
     */
    boolean upload(String destination, String zipName, String... paths);

    /**
     * Downloads the file to user.home destination
     * @param path - of file you want to download
     * @return true - if successful
     */
    boolean download(String path);

    /**
     * Adds meta (key-value pair) to chosen file
     * @param path - of file you want to add meta
     * @param key - key value of meta
     * @param value - value for that key
     */
    void addMeta(String path, String key, String value);

    /**
     * Gets the meta value for provided key. If exists.
     * @param path - of file you want to get meta
     * @param key - key value you want to search with
     */
    void getMeta(String path, String key);

    /**
     * Adds user to current storage
     * @param name - of user
     * @param password - of user
     * @param privilege - of user (ADMIN or GUEST)
     */
    void addUser(String name, String password, UserPrivilege privilege);

    /**
     * Makes directories with given paths
     * @param paths - path of new directory
     * @return true - if successful
     */
    boolean mkDir(String[] paths);

    /**
     * Makes files with given path
     * @param path - path of new file
     */
    void mkFile(String path);

    /**
     * Deletes item at chosen path
     * @param path - of item you want to delete
     */
    void deleteItem(String path);

    /**
     * Checks if current user is logged in
     * @return true - if user is logged in
     */
    boolean isLoggedIn();

    /**
     * Checks if current user is admin
     * @return true or false
     */
    boolean isAdmin();

    /**
     * Checks if given extension is blacklisted
     * @param extension
     * @return true if extension is blacklisted
     */
    boolean isBlacklisted(String extension);

    /**
     * Adds given extension to blacklist
     * @param extension
     */
    void addBlacklisted(String extension);

    /**
     * Removes given extension from blacklist
     * @param extension
     */
    void removeBlacklisted(String extension);

    /**
     * Lists files in directories (if chosen) from selected path
     * @param path - path in which to look
     * @param subdirectories - set true if you want to go into subfolders
     * @param isDir - set true if you want to see only directories
     */
    void lsDir(String path, boolean subdirectories, boolean isDir);

    /**
     * Clears console screen
     */
    void clearScreen();

    /**
     * Displays help of all functions
     */
    void help();

    /**
     * Searches given file name if exists
     * @param fileName
     */
    void search(String fileName);
}
