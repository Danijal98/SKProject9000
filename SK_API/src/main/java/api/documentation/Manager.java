package api.documentation;

public interface Manager {
    Connection connect(String connection);
    Connection initConnection(String DatabaseName);
}
