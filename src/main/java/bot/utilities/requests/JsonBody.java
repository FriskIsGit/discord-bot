package bot.utilities.requests;

import java.util.Iterator;
import java.util.List;

public class JsonBody{
    private final StringBuilder body = new StringBuilder();
    private boolean hasTokens = false;
    private JsonBody(){
    }

    public static JsonBody body(){
        JsonBody jsonBody = new JsonBody();
        jsonBody.body.append('{');
        return jsonBody;
    }

    public JsonBody addPair(String property, String value){
        if(hasTokens){
            body.append(',');
        }
        body.append('"').append(property).append("\":");
        body.append('"').append(value).append('"');
        hasTokens = true;
        return this;
    }
    public JsonBody addPair(String property, boolean value){
        if(hasTokens){
            body.append(',');
        }
        body.append('"').append(property).append("\":");
        body.append(value);
        hasTokens = true;
        return this;
    }
    public JsonBody addPair(String property, float value){
        if(hasTokens){
            body.append(',');
        }
        body.append('"').append(property).append("\":");
        body.append(value);
        hasTokens = true;
        return this;
    }
    public JsonBody addPair(String property, double value){
        if(hasTokens){
            body.append(',');
        }
        body.append('"').append(property).append("\":");
        body.append(value);
        hasTokens = true;
        return this;
    }
    public JsonBody addPair(String property, int value){
        if(hasTokens){
            body.append(',');
        }
        body.append('"').append(property).append("\":");
        body.append(value);
        hasTokens = true;
        return this;
    }
    public JsonBody addPair(String property, JsonBody jsonBody){
        if(hasTokens){
            body.append(',');
        }
        body.append('"').append(property).append("\":");
        body.append(jsonBody.get());
        hasTokens = true;
        return this;
    }
    public JsonBody addPair(String property, List<String> list){
        if(hasTokens){
            body.append(',');
        }
        body.append('"').append(property).append("\":[");
        Iterator<String> iterator = list.iterator();
        int size = list.size();
        int i = 0;
        while(iterator.hasNext()){
            i++;
            body.append('\"').append(iterator.next()).append('\"');
            if(i != size)
                body.append(',');
        }
        body.append(']');
        hasTokens = true;
        return this;
    }

    public String get(){
        body.append('}');
        return body.toString();
    }
    public byte[] getBytes(){
        body.append('}');
        return body.toString().getBytes();
    }

    public void clear(){
        hasTokens = false;
        body.setLength(1);
    }

    @Override
    public String toString(){
        return body.toString();
    }
}
