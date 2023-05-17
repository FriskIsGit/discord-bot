package youtube_lib.downloader.request;

import youtube_lib.model.search.SearchResult;
import youtube_lib.model.search.query.Searchable;

public class RequestSearchable extends Request<RequestSearchable, SearchResult> {

    private final String searchPath;

    public RequestSearchable(Searchable searchable) {
        this.searchPath = searchable.searchPath();
    }

    public String searchPath() {
        return searchPath;
    }

}
