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

    public Connection connect(String path, String username, String password) {
        return new ConnectionImplementation(path, username, password);
    }

}
