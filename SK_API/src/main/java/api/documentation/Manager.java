package api.documentation;

public interface Manager {
    Connection connect(String path, String username, String password);
    Connection initConnection(String DatabaseName);
}
