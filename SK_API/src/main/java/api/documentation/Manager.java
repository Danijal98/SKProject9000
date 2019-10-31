package api.documentation;

public interface Manager {
    Connection connect(String connection, String username, String password);
    Connection initConnection(String DatabaseName);
}
