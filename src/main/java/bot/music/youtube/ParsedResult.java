package bot.music.youtube;

class ParsedResult{
    protected String videoId;
    protected StreamType type;
    protected int formatNumber;

    public ParsedResult(StreamType type, String videoId, int formatNumber){
        this.videoId = videoId;
        this.type = type;
        this.formatNumber = formatNumber;
    }
    public ParsedResult(StreamType type, String videoId){
        this.videoId = videoId;
        this.type = type;
    }
    public ParsedResult(StreamType type){
        this.type = type;
    }
    @Override
    public String toString(){
        return videoId + " " + type + " " + formatNumber;
    }

}
