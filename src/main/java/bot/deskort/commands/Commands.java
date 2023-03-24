package bot.deskort.commands;

import bot.deskort.commands.voice.*;

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
            new EmojiCommand("emoji"),
            new YoutubeCommand("ytinfo", "ytviau", "ytvi", "ytvideo", "ytau", "ytaudio"),
            new StateCommand("state", "vcstate"),
            new RegainCommand("regain"),
            new LogCommand("logs"),
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

    public boolean hasCommand(String alias){
        return commandsMap.containsKey(alias);
    }
    public Command command(String name){
        return commandsMap.get(name);
    }

}
