package youtube_lib.extractor;

import com.alibaba.fastjson.JSONObject;
import youtube_lib.YoutubeException;

import java.util.List;

public interface Extractor {

    JSONObject extractInitialDataFromHtml(String html) throws YoutubeException;

    JSONObject extractPlayerConfigFromHtml(String html) throws YoutubeException;

    List<String> extractSubtitlesLanguagesFromXml(String xml) throws YoutubeException;

    String extractJsUrlFromConfig(JSONObject config, String videoId) throws YoutubeException;

    String extractClientVersionFromContext(JSONObject context);

    int extractIntegerFromText(String text);

    long extractLongFromText(String text);

}
