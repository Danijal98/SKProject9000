package remote.implementation;

import api.documentation.Connection;
import api.documentation.Manager;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

public class ManagerImplementation implements Manager {

    public static ManagerImplementation instance;
    private static final String ACCESS_TOKEN = "zpNANekMULAAAAAAAAAAat6ccjhPrD7yZjYM7_c0rG1UArQl6-2_GERJa60bEZY0";
    private DbxClientV2 client;

    private ManagerImplementation() {
        client = createTestDbxClientV2(ACCESS_TOKEN);
    }

    public static ManagerImplementation getInstance() {
        if (instance == null)
            instance = new ManagerImplementation();
        return instance;
    }

    public Connection connect(String path, String username, String password) {
        return new ConnectionImplementation(path,username,password,client);
    }

    public Connection initConnection(String DatabaseName) {
        return null;
    }

    private static DbxClientV2 createTestDbxClientV2(String accessToken) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder(accessToken).build();
        return new DbxClientV2(config, accessToken);
    }

}
