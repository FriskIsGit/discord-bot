package youtube_lib.downloader.request;

public class RequestWebpage extends Request<RequestWebpage, String>{

    protected final String url;
    private final String method;
    private final String body;

    public RequestWebpage(String url) {
        this(url, "GET", null);
    }

    public RequestWebpage(String url, String method, String body) {
        this.url = url;
        this.method = method;
        this.body = body;
    }

    public String getDownloadUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }
}
