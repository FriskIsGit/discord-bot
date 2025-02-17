package bot.commands.filebin;

import kong.unirest.HttpResponse;

//if response is present and code is 200 or 201 - display url
//if response is present but code is other than 200 and 201 - display response code and error message
//if response is not present - display no response
public class BinPostResult {
    public boolean exceptionThrown;
    public String url;
    public HttpResponse<String> response;

    public static BinPostResult ok(String url, HttpResponse<String> response) {
        return new BinPostResult(false, url, response);
    }

    public static BinPostResult fail() {
        return new BinPostResult(true, "", null);
    }

    private BinPostResult(boolean exception, String url, HttpResponse<String> response) {
        exceptionThrown = exception;
        this.url = url;
        this.response = response;
    }
}
