package bot.music.youtube;

import bot.deskort.Commands;
import bot.deskort.MessageProcessor;

public class YoutubeRequestParser{
    private final String[] requestTerms;
    public YoutubeRequestParser(String requestText){
        int prefixOffset = MessageProcessor.PREFIX_OFFSET;
        this.requestTerms = Commands.splitIntoTerms(requestText, prefixOffset);
    }

    public ParsedResult parse(){
        if(requestTerms.length < 2){
            return new ParsedResult(StreamType.NONE);
        }
        StreamType type = null;
        //command <id> <num>

        char typeChar = requestTerms[0].charAt(2);
        if(typeChar == 'i'){
            type = StreamType.INFO;
        }else if(typeChar == 'a'){
            type = StreamType.AUDIO;
        }else if(typeChar == 'v'){
            type = StreamType.VIDEO;
        }

        //entire links for convenience
        if(requestTerms[1].startsWith("www") || requestTerms[1].startsWith("http")){
            requestTerms[1] = Youtube.getVideoId(requestTerms[1]);
        }
        //info requests should have only two terms
        if (type == StreamType.INFO){
            ParsedResult result = new ParsedResult(StreamType.INFO, requestTerms[1]);
            System.out.println(result);
            return result;
        }

        //checks if video/vi is followed up with audio/au or the other way around
        if(type == StreamType.VIDEO || type == StreamType.AUDIO){
            for(int i = 3;i<requestTerms[0].length(); i++){
                char character = requestTerms[0].charAt(i);
                if(character == 'a' || character == 'v'){
                    type = StreamType.VIDEO_AUDIO;
                    break;
                }
            }
        }

        //parse videoId and formatNumber
        int formatNumber;
        try{
            formatNumber = Integer.parseInt(requestTerms[2]);
        }catch (NumberFormatException nfExc){
            return new ParsedResult(StreamType.NONE);
        }
        ParsedResult result = new ParsedResult(type, requestTerms[1], formatNumber);
        System.out.println(result);
        return result;
    }
}
