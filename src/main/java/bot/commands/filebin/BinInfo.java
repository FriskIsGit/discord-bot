package bot.commands.filebin;

import org.jetbrains.annotations.NonNls;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

//wrapper and parser
public class BinInfo{
    private static final String FILE_BIN_NET = "https://filebin.net";
    public String fileName, id;
    public String contentType, contentSize;
    public String md5, sha256;
    public String expires;
    public long allBytes = -1;

    public BinInfo(@NonNls JSONObject json){
        Set<String> objects = json.keySet();
        if(objects.contains("files")){
            try{
                JSONArray files = json.getJSONArray("files");
                if(files.length() < 1) return;

                JSONObject fileInfo = (JSONObject) files.get(0);
                fileName    = fileInfo.getString("filename");
                contentType = fileInfo.getString("content-type");
                contentSize = fileInfo.getString("bytes_readable");
                allBytes = fileInfo.getLong("bytes");
                //bytes_readable
                md5         = fileInfo.getString("md5");
                sha256      = fileInfo.getString("sha256");
            }catch (JSONException jsonExc){
                jsonExc.printStackTrace();
            }
        }
        if(objects.contains("bin")){
            JSONObject binObj = json.getJSONObject("bin");
            id = binObj.getString("id");
            expires = binObj.getString("expired_at_relative");
        }

    }
    public BinInfo(@NonNls String responseBody){
        this(new JSONObject(responseBody));
    }

    public String getURL(){
        return FILE_BIN_NET + '/' + id + '/' + fileName;
    }
}
