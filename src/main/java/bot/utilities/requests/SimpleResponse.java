package bot.utilities.requests;

import bot.utilities.Option;
import bot.utilities.StreamUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import java.io.IOException;
import java.io.InputStream;

public class SimpleResponse{
    public int code;
    public String body;

    public SimpleResponse(int code, String body){
        this.code = code;
        this.body = body;
    }

    public static Option<SimpleResponse> performRequest(Request request, JsonBody jsonBody) {
        if(jsonBody != null)
            request.bodyByteArray(jsonBody.getBytes());

        InputStream responseAsStream = null;
        try{
            Response response = request.execute();
            HttpResponse consumedResponse = response.returnResponse();
            int code = consumedResponse.getStatusLine().getStatusCode();
            responseAsStream = consumedResponse.getEntity().getContent();
            SimpleResponse simpleResponse = new SimpleResponse(code, StreamUtil.streamToString(responseAsStream));
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

    public static Option<SimpleResponse> performRequest(Request request) {
        return performRequest(request, null);
    }

    @Override
    public String toString(){
        return "[" + code + "]\n" + body;
    }
}
