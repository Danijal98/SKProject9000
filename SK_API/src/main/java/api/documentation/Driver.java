package api.documentation;

public class Driver {

    private String connectionType;

    public Driver(String connectionType){
        this.connectionType = connectionType;
    }

    public Manager getManager(){
        if(connectionType.equals("local")){
            //TODO
            //return ManagerImplementation
        }else if(connectionType.equals("remote")){
            //TODO
        }
        return null;
    }

}
