package api.documentation;

import java.util.HashMap;
import java.util.Map;

abstract public class File {

    private Map<String,String> data;

    public File(String path, String isVisible) {
        data = new HashMap<String, String>();
        data.put("path",path);
        data.put("isVisible",isVisible);//y or n
    }

    public abstract void setMetaData(String key, String value);



}
