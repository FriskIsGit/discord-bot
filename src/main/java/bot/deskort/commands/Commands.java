package bot.deskort.commands;

import bot.deskort.commands.voice.*;

import java.util.ArrayList;
import java.util.HashMap;

//singleton
final public class Commands{
    private static Commands instance;

    //it can be public since the map will be already populated
    public final Command[] commands = {
            new BanCommand("ban"),
            new UnbanCommand("unban"),
            new PurgeCommand("purge"),
            new PlayCommand("play", "p"),
            new JoinCommand("join", "j"),
            new HelpCommand("help", "commands"),
            new ModifyCommand("modify"),
            new UptimeCommand("uptime"),
            new LeaveCommand("leave", "l"),
            new MemoryCommand("memory", "mem    stat","mempanel", "memuse"),
            new StopCommand("stop"),
            new SongsCommand("songs", "tracks"),
            new TokenCommand("token", "gentoken"),
            new HashCommand("hash", "sha256", "sha512", "sha1", "md5", "sha224", "sha384"),
            new ShutdownCommand("shutdown"),
            new AbortCommand("abort"),
            new LoopCommand("loop"),
            new HttpCommand( "httpcat", "http"),
            new LengthCommand("len", "length"),
            new QueueCommand("queue", "q"),
            new SkipCommand("skip"),
            new WarpCommand("warp"),
            new AuditLogCommand("auditlog"),
            new YoutubeCommand("ytinfo", "ytviau", "ytvi", "ytvideo", "ytau", "ytaudio"),
            new StateCommand("state", "vcstate"),
            new RegainCommand("regain"),
            new LogCommand("logs"),
            new GCCommand("gc"),
            new ClearSongsCommand("clearsongs", "clrsongs"),
            new FileHashCommand("hashfile", "file"),
            new InviteCommand("invite"),
            new RoleCommand("role", "roles"),
            new SudoCommand("sudo"),
            new URLCommand("url" , "link", "links"),
    };
    private final HashMap<String, Command> commandsMap = new HashMap<>(commands.length);

    public Commands(){
        populateMap();
    }

    public static Commands get(){
        if(instance == null){
            instance = new Commands();
        }
        return instance;
    }

    public static String mergeTerms(String[] terms, int fromIndex){
        if(terms.length == 0){
            return "";
        }
        int totalLength = 0;
        for (int i = fromIndex; i < terms.length; i++){
            totalLength += terms[i].length();
        }
        StringBuilder str = new StringBuilder(totalLength);
        for (int i = fromIndex; i < terms.length; i++){
            String term = terms[i];
            str.append(term);
        }
        return str.toString();
    }
    public static String mergeTerms(String[] terms){
        return mergeTerms(terms, 0);
    }

    private void populateMap(){
        for(Command command : commands){
            for(String name : command.aliases){
                commandsMap.put(name, command);
            }
        }
    }
    public void addToMap(Command command){
        for(String name : command.aliases){
            commandsMap.put(name, command);
        }
    }

    public boolean hasCommand(String alias){
        return commandsMap.containsKey(alias);
    }
    public Command command(String name){
        return commandsMap.get(name);
    }
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

    /**
     * Splits character sequence where the delimiter is any number of whitespaces.
     * During parsing following rules apply: <br>
     * - any trailing or leading whitespaces are ignored unless they're placed inside quotes <br>
     * - if quotation marks are used they should be non-empty and de-nested otherwise,
     * they'll be treated as a single term. <br>
     * - if they are never closed they'll be treated as part of another term or a single term.
     * @param text string to parse
     * @param fromIndex index to begin splitting from
     * @return array of terms split by aforementioned rules,
     * the array returned is guaranteed to have length of at least one
     */
    public static String[] splitIntoTerms(String text, int fromIndex){
        if(fromIndex < 0 || text.length() <= fromIndex){
            return new String[]{""};
        }
        ArrayList<String> terms = new ArrayList<>(8);
        char[] arr = text.toCharArray();
        int len = arr.length;
        boolean hasToken = false, inQuotes = false;
        for (int i = fromIndex; i<len; i++){
            boolean isLast = i == len-1;
            switch (arr[i]){
                case ' ':
                    if(hasToken && !inQuotes){
                        String term = text.substring(fromIndex, i);
                        terms.add(term);
                        hasToken = false;
                    }
                    //never closed
                    else if(inQuotes && isLast){
                        //go back since the initial assumption was incorrect
                        i = fromIndex;
                        inQuotes = false;
                        break;
                    }
                    break;
                case '"':
                    if(hasToken && !inQuotes){
                        break;
                    }
                    if(inQuotes){
                        //wants to close quotes
                        if(isLast || arr[i+1] == ' '){
                            //"quote";"quote" ;
                            String qTerm = text.substring(fromIndex+1, i);
                            if(!qTerm.isEmpty()){
                                terms.add(qTerm);
                                hasToken = false;
                            }
                        }else{
                            //another quote was opened with a proceeding non-whitespace char
                            i = fromIndex;
                        }
                        //carry on
                        //"quote"what;"quote""what";
                        inQuotes = false;
                        break;
                    }
                    inQuotes = true;
                    hasToken = true;
                    fromIndex = i;
                    break;
                default:
                    if(hasToken && isLast){
                        if(inQuotes){
                            //unclosed
                            i = fromIndex;
                            inQuotes = false;
                            break;
                        }
                        String term = text.substring(fromIndex, len);
                        terms.add(term);
                        //end
                        break;
                    }
                    if(hasToken){
                        break;
                    }
                    hasToken = true;
                    if(isLast){
                        //last singular char token
                        terms.add(Character.toString(arr[i]));
                    }
                    fromIndex = i;
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

    public static String[] splitIntoTerms(String text){
        return splitIntoTerms(text, 0);
    }

    public static String[] shrink(String[] arr, int fromIndex){
        if(fromIndex < 0 || arr.length <= fromIndex){
            return new String[0];
        }
        String[] freshArr = new String[arr.length - fromIndex];
        for (int i = fromIndex, f = 0; i < arr.length; i++, f++){
            freshArr[f] = arr[i];
        }
        return freshArr;
    }
}
