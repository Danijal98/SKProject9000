package local.implemetation;

import api.documentation.Connection;
import api.documentation.Manager;

public class ManagerImplementation implements Manager {

    public static ManagerImplementation instance;

    private ManagerImplementation() {
    }

    public static ManagerImplementation getInstance() {
        if (instance == null)
            instance = new ManagerImplementation();
        return instance;
    }

    public Connection connect(String connection, String username, String password) {
        return new ConnectionImplementation(connection, username, password);
    }

    public Connection initConnection(String DatabaseName) {
        return null;
    }

}
