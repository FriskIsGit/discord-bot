package bot.utilities.requests;

import java.net.URLEncoder;

//facilitates building request parameters and encodes them
public class Params{

    private final StringBuilder str = new StringBuilder();
    private boolean first = true;

    private Params(){
    }

    public static Params New(){
        return new Params();
    }

    public Params addPair(String key, String value){
        if(first){
            first = false;
            str.append('?');
        }else{
            str.append('&');
        }
        str.append(encode(key)).append('=').append(encode(value));
        return this;
    }

    public <T> Params addPair(String key, T val){
        return addPair(key, String.valueOf(val));
    }

    public String get(){
        return str.toString();
    }

    public void clear(){
        first = true;
        str.setLength(0);
    }

    private static String encode(String toEncode){
        try{
            return URLEncoder.encode(toEncode, "utf-8");
        }catch (Exception ignored){
        }
        throw new IllegalStateException("Unreachable code");
    }

    @Override
    public String toString(){
        return str.toString();
    }
}
