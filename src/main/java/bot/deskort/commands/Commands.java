package bot.deskort.commands;

import bot.deskort.commands.voice.*;
import net.dv8tion.jda.api.entities.Invite;

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
            new HelpCommand("help"),
            new ModifyCommand("modify"),
            new UptimeCommand("uptime"),
            new LeaveCommand("leave", "l"),
            new MemoryCommand("memory", "memstat","mempanel", "memuse"),
            new StopCommand("stop"),
            new SongsCommand("songs", "tracks"),
            new TokenCommand("token", "gentoken"),
            new HashCommand("hash", "sha256", "sha512", "sha1", "md5", "sha224", "sha384"),
            new ShutdownCommand("shutdown"),
            new AbortCommand("abort"),
            new LoopCommand("loop"),
            new HttpCommand("cat", "httpcat", "http"),
            new LengthCommand("len", "length"),
            new QueueCommand("q", "queue"),
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
    public static String[] splitIntoTerms(String text){
        return splitIntoTerms(text, 0);
    }

}