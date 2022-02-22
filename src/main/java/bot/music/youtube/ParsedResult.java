package bot.music.youtube;

class ParsedResult{
    protected String videoId;
    protected StreamType type;
    protected int formatNumber;

    public ParsedResult(String videoId, StreamType type, int formatNumber){
        this.videoId = videoId;
        this.type = type;
        this.formatNumber = formatNumber;
    }
    @Override
    public String toString(){
        return videoId + " " + type + " " + formatNumber;
    }

}
