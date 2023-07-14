package bot.commands.lyrics;

import com.alibaba.fastjson.JSONObject;

public class SongInfo{
    public int id, pageViews;
    public String fullTitle, lyricsURL, thumbnailURL, release;

    //parses "result" key
    public static SongInfo fromJson(JSONObject json){
        SongInfo info = new SongInfo();
        //artist_names is not an array
        info.fullTitle = json.getString("artist_names") + " - " + json.getString("title");
        info.thumbnailURL = json.getString("header_image_thumbnail_url");
        info.lyricsURL = json.getString("url");
        info.id = json.getInteger("id");
        info.release = json.getString("release_date_for_display");
        JSONObject stats = json.getJSONObject("stats");
        if(stats != null && stats.containsKey("pageviews")){
            info.pageViews = stats.getInteger("pageviews");
        }
        return info;
    }

    @Override
    public String toString(){
        return "SongInfo{" +
                "id=" + id +
                ", fullTitle='" + fullTitle + '\'' +
                ", lyricsURL='" + lyricsURL + '\'' +
                ", thumbnailURL='" + thumbnailURL + '\'' +
                ", release=" + release +
                '}';
    }
}
