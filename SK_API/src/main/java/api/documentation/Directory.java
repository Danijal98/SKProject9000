package api.documentation;

import java.util.ArrayList;

abstract public class Directory extends File {

    private ArrayList<File> files;

    public Directory(String path, String isVisible) {
        super(path, isVisible);
    }

    public void addFile(File file){
        files.add(file);
    }

}
