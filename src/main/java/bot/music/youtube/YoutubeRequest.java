package bot.music.youtube;

public class YoutubeRequest{
    public StreamType type;
    public String videoId;
    public int formatNumber;

    public YoutubeRequest(StreamType type, String videoId, int formatNumber){
        this.videoId = videoId;
        this.type = type;
        this.formatNumber = formatNumber;
    }
    public YoutubeRequest(StreamType type, String videoId){
        this.videoId = videoId;
        this.type = type;
    }
    public YoutubeRequest(StreamType type){
        this.type = type;
    }
    @Override
    public String toString(){
        return videoId + " " + type + " " + formatNumber;
    }

}
