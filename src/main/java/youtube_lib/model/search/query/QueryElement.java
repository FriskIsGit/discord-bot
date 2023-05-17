package youtube_lib.model.search.query;

import youtube_lib.model.search.SearchResultElement;

public interface QueryElement extends SearchResultElement{

    QueryElementType type();

}
