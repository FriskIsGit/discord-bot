package bot.deskort;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Commands{
    final static HashMap<String, RequestFunction> COMMANDS_TO_FUNCTIONS = new HashMap<String, RequestFunction>() {{

        put("logs",        new RequestFunction(MessageProcessor::logsRequest,       true));
        put("shutdown",    new RequestFunction(MessageProcessor::shutdownRequest,   true));
        put("abort",       new RequestFunction(MessageProcessor::abortRequest,      true));

        put("ban",         new RequestFunction(MessageProcessor::banRequest,        false));
        put("unban",       new RequestFunction(MessageProcessor::unbanRequest,      false));
        put("warp",        new RequestFunction(MessageProcessor::warpRequest,       false));

        put("join",        new RequestFunction(MessageProcessor::joinRequest,       false));
        put("leave",       new RequestFunction(MessageProcessor::leaveRequest,      false));
        put("loop",        new RequestFunction(MessageProcessor::loopRequest,       false));
        RequestFunction    playRequest = new RequestFunction(MessageProcessor::playRequest,       false);
        put("play",        playRequest);
        put("p",           playRequest);
        put("stop",        new RequestFunction(MessageProcessor::stopRequest,       false));
        RequestFunction    queueRequest = new RequestFunction(MessageProcessor::queueRequest,      true);
        put("queue",       queueRequest);
        put("q",           queueRequest);
        put("skip",        new RequestFunction(MessageProcessor::skipRequest,       true));

        put("regain",      new RequestFunction(MessageProcessor::regain,            false));
        put("yt",          new RequestFunction(MessageProcessor::youtubeRequest,    false));
        RequestFunction    tracksRequestFunction = new RequestFunction(MessageProcessor::tracksRequest, false);
        put("tracks",      tracksRequestFunction);
        put("songs",       tracksRequestFunction);

        put("purge",       new RequestFunction(MessageProcessor::purgeRequest,      false));
        put("uptime",      new RequestFunction(MessageProcessor::uptimeRequest,     true));
        put("help",        new RequestFunction(MessageProcessor::helpRequest,       false));
        put("gentoken",    new RequestFunction(MessageProcessor::genTokenRequest,   true));
        RequestFunction    lengthRequest = new RequestFunction(MessageProcessor::lengthRequest, true);
        put("length",      lengthRequest);
        put("len",         lengthRequest);
        RequestFunction    httpCatRequest = new RequestFunction(MessageProcessor::httpCatRequest,   true);
        put("httpcat",     httpCatRequest);
        put("http",        httpCatRequest);
        put("cat",         httpCatRequest);
        put("auditlog",    new RequestFunction(MessageProcessor::auditLogRequest,   false));
        put("vcstate",     new RequestFunction(MessageProcessor::hasActiveConnectionRequest,   false));
        put("emoji",       new RequestFunction(MessageProcessor::emojiId,   true));


        RequestFunction    memoryRequestFunction = new RequestFunction(MessageProcessor::memoryRequest, false);
        put("memstat",     memoryRequestFunction);
        put("mempanel",    memoryRequestFunction);
        put("memuse",      memoryRequestFunction);
        put("clrsongs",    new RequestFunction(MessageProcessor::clearSongsRequest, true));
        put("gc",          new RequestFunction(MessageProcessor::GCRequest,         true));

        RequestFunction    hashRequestFunction = new RequestFunction(MessageProcessor::stringHashRequest, false);
        put("hash",        hashRequestFunction);


    }};

    public static String[] doubleTermSplit(String commandText){
        return doubleTermSplit(commandText,0);
    }
    //always returns an array of length 2
    public static String[] doubleTermSplit(String commandText, int fromIndex){
        if(fromIndex < 0 || commandText.length() <= fromIndex){
            return new String[]{"",""};
        }
        ArrayList<String> terms = new ArrayList<>(2);
        char[] arr = commandText.toCharArray();
        boolean lookingForWhitespace = true;
        for (int i = fromIndex; i < arr.length; i++){
            if(lookingForWhitespace && arr[i] == ' '){
                terms.add(commandText.substring(fromIndex, i));
                lookingForWhitespace = false;
            }
            else if(!lookingForWhitespace && arr[i] != ' '){
                terms.add(commandText.substring(i));
                break;
            }
        }
        if(terms.size() == 0){
            return new String[]{commandText.substring(fromIndex), ""};
        }
        if (terms.size() == 1){
            return new String[]{terms.get(0), ""};
        }
        String[] res = new String[2];
        final int[] index = {0};
        terms.forEach((el) -> res[index[0]++] = el);
        return res;
    }

    //returns an array of length at least 1
    public static String[] splitIntoTerms(String text, int fromIndex){
        if(fromIndex < 0 || text.length() <= fromIndex){
            return new String[]{""};
        }
        ArrayList<String> terms = new ArrayList<>(8);
        text = text.trim();
        char[] arr = text.toCharArray();
        boolean expectingWhitespace = true;
        for (int i = fromIndex; i<arr.length; i++){

            if(expectingWhitespace && arr[i] == ' '){
                terms.add(text.substring(fromIndex,i));
                expectingWhitespace = false;
            }else if(!expectingWhitespace && arr[i] != ' '){
                fromIndex = i;
                expectingWhitespace = true;
            }
            if(i == arr.length-1){
                terms.add(text.substring(fromIndex,arr.length));
                break;
            }
        }
        if(terms.size() == 0){
            return new String[]{""};
        }
        String[] res = new String[terms.size()];
        final int[] index = {0};
        terms.forEach((el) -> res[index[0]++] = el);
        return res;
    }

}
