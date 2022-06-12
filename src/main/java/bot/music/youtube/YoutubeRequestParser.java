package bot.music.youtube;

import bot.deskort.Commands;
import bot.deskort.MessageProcessor;

import static bot.music.youtube.StreamType.*;

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
            type = INFO;
        }else if(typeChar == 'a'){
            type = AUDIO;
        }else if(typeChar == 'v'){
            type = VIDEO;
        }

        //entire links for convenience
        if(requestTerms[1].startsWith("www") || requestTerms[1].startsWith("http")){
            requestTerms[1] = Youtube.getVideoId(requestTerms[1]);
        }
        //info requests should have only two terms
        if (type == INFO){
            ParsedResult result = new ParsedResult(INFO, requestTerms[1]);
            System.out.println(result);
            return result;
        }

        //checks if video/vi is followed up with audio/au or the other way around
        if(type == VIDEO || type == AUDIO){
            for(int i = 3;i<requestTerms[0].length(); i++){
                char character = requestTerms[0].charAt(i);
                if(character == 'a' || character == 'v'){
                    type = VIDEO_AUDIO;
                    break;
                }
            }
        }

        //parse videoId and formatNumber
        int formatNumber;
        try{
            formatNumber = Integer.parseInt(requestTerms[2]);
        }catch (NumberFormatException nfExc){
            return new ParsedResult(NONE);
        }
        ParsedResult result = new ParsedResult(type, requestTerms[1], formatNumber);
        System.out.println(result);
        return result;
    }
}
