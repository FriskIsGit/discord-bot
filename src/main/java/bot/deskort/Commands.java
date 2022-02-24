package bot.deskort;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Commands{
    final static HashMap<String, RequestFunction> COMMANDS_TO_FUNCTIONS = new HashMap<String, RequestFunction>() {{

        put("logs",        new RequestFunction(MessageProcessor::logsRequest,       true));
        put("shutdown",    new RequestFunction(MessageProcessor::shutdownRequest,   true));

        put("ban",         new RequestFunction(MessageProcessor::banRequest,        false));
        put("unban",       new RequestFunction(MessageProcessor::unbanRequest,      false));
        put("join",        new RequestFunction(MessageProcessor::joinRequest,       false));
        put("warp",        new RequestFunction(MessageProcessor::warpRequest,       false));
        put("play",        new RequestFunction(MessageProcessor::playRequest,       false));
        put("queue",       new RequestFunction(MessageProcessor::queueRequest,      true));
        put("stop",        new RequestFunction(MessageProcessor::stopRequest,       false));
        put("leave",       new RequestFunction(MessageProcessor::leaveRequest,      false));
        put("loop",        new RequestFunction(MessageProcessor::loopRequest,       false));
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
        put("test",        new RequestFunction(MessageProcessor::test,              false));

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
        ArrayList<String> terms = new ArrayList<>(2);
        char[] arr = commandText.toCharArray();
        boolean lookingForWhitespace = true;
        for (int i = Bot.PREFIX_OFFSET; i < arr.length; i++){
            if(lookingForWhitespace && arr[i] == ' '){
                terms.add(commandText.substring(Bot.PREFIX_OFFSET, i));
                lookingForWhitespace = false;
            }
            else if(!lookingForWhitespace && arr[i] != ' '){
                terms.add(commandText.substring(i));
                break;
            }
        }
        if(terms.size() == 0){
            return new String[]{commandText.substring(Bot.PREFIX_OFFSET), ""};
        }
        if (terms.size() == 1){
            return new String[]{terms.get(0), ""};
        }
        String[] res = new String[2];
        final int[] index = {0};
        terms.forEach((el) -> res[index[0]++] = el);
        return res;
    }

    public static String[] splitIntoTerms(String text, int fromIndex){
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
