package bot.utilities.requests;

import bot.utilities.Option;
import bot.utilities.StreamUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleResponse{
    static {
        //cookie warning fix
        Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
    }
    public int code;
    public String body;
    public final Header[] headers;

    public SimpleResponse(int code, String body){
        this.code = code;
        this.body = body;
        this.headers = new Header[0];
    }
    public SimpleResponse(int code, String body, Header[] headers){
        this.code = code;
        this.body = body;
        this.headers = headers;
    }

    public static Option<SimpleResponse> performRequest(Request request, JsonBody jsonBody) {
        if(jsonBody != null)
            request.bodyByteArray(jsonBody.getBytes());

        InputStream responseAsStream = null;
        try{
            Response response = request.execute();
            HttpResponse consumedResponse = response.returnResponse();
            Header[] headers = consumedResponse.getAllHeaders();
            int code = consumedResponse.getStatusLine().getStatusCode();
            responseAsStream = consumedResponse.getEntity().getContent();
            SimpleResponse simpleResponse = new SimpleResponse(code, StreamUtil.streamToString(responseAsStream), headers);
            return Option.of(simpleResponse);
        }catch (IOException e){
            e.printStackTrace();
            return Option.none();
        }finally{
            if(responseAsStream != null){
                try{
                    responseAsStream.close();
                }catch (IOException ignored){
                }
            }
        }
    }

    public Header getHeader(String name){
        for(Header header : headers){
            if(header.getName().equalsIgnoreCase(name)){
                return header;
            }
        }
        return null;
    }

    public static Option<SimpleResponse> performRequest(Request request) {
        return performRequest(request, null);
    }

    @Override
    public String toString(){
        return "[" + code + "]\n" + body;
    }
}
