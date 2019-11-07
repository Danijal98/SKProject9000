package api.documentation;

public interface Manager {

    /**
     * Establishes connection with the selected database.
     *
     * @param path     - path of the database
     * @param username - username for login
     * @param password - password for login
     * @return - Returns connection for the requested storage
     */
    Connection connect(String path, String username, String password);
}
