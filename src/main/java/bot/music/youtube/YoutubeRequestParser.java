package bot.music.youtube;

import bot.deskort.MessageProcessor;

public class YoutubeRequestParser{
    private final int prefixOffset;
    String request;
    int length;
    public YoutubeRequestParser(String request){
        this.prefixOffset = MessageProcessor.PREFIX_OFFSET;
        this.request = request;
        this.length = request.length();
    }

    public ParsedResult parse(){
        StreamType type = null;
        //>yttype id num
        char typeChar = request.charAt(prefixOffset + 2);

        if(typeChar == 'i'){
            type = StreamType.INFO;
        }else if(typeChar == 'a'){
            type = StreamType.AUDIO;
        }else if(typeChar == 'v'){
            type = StreamType.VIDEO;
        }

        int i = prefixOffset+3;
        if(type == StreamType.VIDEO){
            for(;i<length; i++){
                char character = request.charAt(i);
                if(character == ' '){
                    i--;
                    break;
                }else if(character == 'a'){
                    type = StreamType.VIDEO_AUDIO;
                    break;
                }
            }
        }
        //parse videoId and formatNumber
        int whitespaces = 0, indexId = -1, formatNumber = -1;
        String videoId = null;
        for (;i<length; i++){
            char character = request.charAt(i);
            //videoId begin
            if(whitespaces == 0 && character == ' ' && i+1<length && request.charAt(i+1) != ' '){
                indexId = i + 1;
                if (type == StreamType.INFO){
                    videoId = request.substring(indexId);
                    ParsedResult result = new ParsedResult(videoId, type, formatNumber);
                    System.out.println(result);
                    return result;
                }
                whitespaces++;
            }
            //videoId end
            else if(whitespaces == 1 && character == ' '){
                whitespaces++;
                videoId = request.substring(indexId, i);
            }
            //char before formatNumberBegin
            else if(whitespaces > 1 && character != ' '){
                String numAsStr = request.substring(i);
                formatNumber = Integer.parseInt(numAsStr);
                break;
            }
        }
        ParsedResult result = new ParsedResult(videoId,type,formatNumber);
        System.out.println(result);
        return result;
    }
}
