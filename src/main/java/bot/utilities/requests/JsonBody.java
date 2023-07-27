package bot.utilities.requests;

import java.util.Iterator;
import java.util.List;

/**
 * Simple json body builder with a fluent design.
 * Based on pairs of properties and values
 * Double quotation marks or reverse slash characters in string values are escaped before put into the body.
 */
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
        body.append('"').append(escape(value)).append('"');
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
    public JsonBody addPair(String property, long value){
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
            String el = escape(iterator.next());
            body.append('\"').append(el).append('\"');
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

    private static String escape(String str){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            switch (c){
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '"':
                    builder.append("\\\"");
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    @Override
    public String toString(){
        return body.toString();
    }
}
