package bot.commands;

import bot.commands.filebin.FileBinCommand;
import bot.commands.lyrics.LyricsCommand;
import bot.commands.voice.*;
import bot.commands.ai.AICommand;

import java.util.ArrayList;
import java.util.HashMap;

//singleton
public final class Commands{
    private static Commands instance;

    //it can be public since the map will be already populated
    public final Command[] commands = {
            new BanCommand("ban"),
            new UnbanCommand("unban"),
            new KickCommand("kick"),
            new PurgeCommand("purge"),
            new PlayCommand("play", "p"),
            new JoinCommand("join", "j"),
            new HelpCommand("help", "commands"),
            new ManageCommand("manage"),
            new UptimeCommand("uptime"),
            new LeaveCommand("leave", "l"),
            new MemoryCommand("memory", "mem", "memstat","mempanel", "memuse"),
            new StopCommand("stop"),
            new SongsCommand("songs", "tracks"),
            new TokenCommand("token", "gentoken"),
            new HashCommand("hash", "sha256", "sha512", "sha1", "md5", "sha224", "sha384"),
            new ShutdownCommand("shutdown"),
            new AbortCommand("abort"),
            new LoopCommand("loop"),
            new HttpCommand( "httpcat", "http", "cat"),
            new LengthCommand("len", "length"),
            new QueueCommand("queue", "q"),
            new SkipCommand("skip"),
            new WarpCommand("warp"),
            new AuditLogCommand("auditlog"),
            new YoutubeCommand("ytinfo", "ytviau", "ytvi", "ytvideo", "ytau", "ytaudio"),
            new StateCommand("state", "vcstate"),
            new RegainCommand("regain"),
            new LogCommand("logs"),
            new FileHashCommand("hashfile", "file", "filehash"),
            new InviteCommand("invite", "invites"),
            new RoleCommand("role", "roles"),
            new SudoCommand("sudo"),
            new URLCommand("url" , "link", "links"),
            new CompareCommand("compare", "comp", "diff"),
            new ConvertCommand("convert"),
            new AverageCommand("average", "avg"),
            new AICommand("openai", "ai21"),
            new FileBinCommand("filebin", "fb"),
            new FormatCommand("format"),
            new LyricsCommand("lyrics", "lyr", "ryricsu", "ryrics"),
            new HaltCommand("halt"),
            new RussianCommand("rus", "ru", "russian"),
            new Base64Command("enc64", "dec64", "de64", "en64", "un64"),
            new ChannelCommand("vc"),
            new ConfigCommand("config"),
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

    //from inclusive, to exclusive
    public static String mergeTerms(String[] terms, int fromIndex, int toIndex){
        if(terms.length == 0){
            return "";
        }
        int totalLength = 0;
        for (int i = fromIndex; i < toIndex; i++){
            totalLength += terms[i].length() + 1;
        }
        StringBuilder str = new StringBuilder(totalLength);
        for (int i = fromIndex; i < toIndex; i++){
            str.append(terms[i]);
            if(i != toIndex - 1)
                str.append(' ');
        }
        return str.toString();
    }
    public static String mergeTerms(String[] terms){
        return mergeTerms(terms, 0, terms.length);
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
     * Splits character sequence where the delimiter is any number of whitespaces. <br>
     * <b>During parsing following rules apply:</b> <br>
     * <ol>
     *     <li>Any trailing or leading whitespaces are ignored unless placed inside quotes.</li>
     *     <li>Quotation mark rules:</li>
     *     <ul>
     *         <li>should be non-empty and de-nested otherwise, they'll become a single term.</li>
     *         <li>if never closed they'll become part of another term or a single term.</li>
     *     </ul>
     *     <li>Grave accent rules:</li>
     *     <ul>
     *          <li>accept all characters placed inside any valid outer two as one argument</li>
     *          <li>opening grave accent must be preceded with a whitespace,
     *              otherwise it will be treated as part of the current token</li>
     *          <li>closing grave accent must either be followed by a whitespace or the text should end with a grave,
     *              otherwise it will be treated as part of the current or next token</li>
     *          <li>if grave accent is opened after a quote was opened then the grave will be part of the quote term</li>
     *          <li>there can only be one grave term in given text, since only the outer graves are considered</li>
     *     </ul>
     * </ol>
     * @param fromIndex index to begin splitting from
     * @param text string to parse
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
        boolean triedGrave = false;
        for (int i = fromIndex; i<len; i++){
            boolean isLast = i == len-1;
            char c = arr[i];
            switch (c){
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
                    if(!triedGrave && c == '`'){
                        if(!hasToken){
                            triedGrave = true;
                            int grave = lastValidGrave(arr);
                            if(grave != -1 && grave != i){
                                String graveTerm = text.substring(i+1, grave);
                                terms.add(graveTerm);
                                i = grave;
                                break;
                            }
                        }
                    }
                    if(isLast && hasToken){
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
                        terms.add(Character.toString(c));
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

    //looks for a valid closing grave, if not found returns -1
    private static int lastValidGrave(char[] arr){
        int lastIndex = arr.length - 1;
        for (int i = lastIndex; i > -1; i--){
            if(arr[i] == '`'){
                if(i == lastIndex)
                    return lastIndex;
                if(arr[i+1] == ' ')
                    return i;
            }
        }
        return -1;
    }

    public static String[] splitIntoTerms(String text){
        return splitIntoTerms(text, 0);
    }

    public static String[] shrink(String[] arr, int fromIndex){
        if(fromIndex < 0 || arr.length <= fromIndex){
            return new String[0];
        }
        String[] freshArr = new String[arr.length - fromIndex];
        System.arraycopy(arr, fromIndex, freshArr, 0, freshArr.length);
        return freshArr;
    }
}
