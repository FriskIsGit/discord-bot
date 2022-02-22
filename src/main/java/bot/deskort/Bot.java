package bot.deskort;

import bot.deskort.emergency.EmergencyListener;
import bot.music.AudioPlayer;
import org.json.JSONException;
import org.json.JSONObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import bot.utilities.Actions;
import bot.utilities.Channels;
import bot.utilities.FileSeeker;
import bot.utilities.Servers;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Bot{
    private Bot(){}
    private static String TOKEN;
    public static long BOT_ID;
    private static long LAUNCH_TIME = 0;
    public final static Set<Long> AUTHORIZED_USERS = new HashSet<>();
    public static String PREFIX = ">";
    public static int PREFIX_OFFSET = 1;

    private static JDABuilder jdaBuilder;
    private static JDA jdaInterface;
    private static Actions actions;
    private static Servers servers;
    private static Channels channels;
    //private static bot.utilities.Permissions permissions;

    public static boolean initialize() throws InterruptedException{
        if(jdaBuilder == null){
            loadConfig();
            jdaBuilder = JDABuilder.createDefault(TOKEN);
            jdaBuilder.addEventListeners(new EmergencyListener(jdaBuilder));
            //voice limits
            jdaBuilder.enableCache(CacheFlag.VOICE_STATE);
            //jdaBuilder.disableCache(CacheFlag.MEMBER_OVERRIDES);
            try{
                jdaInterface = jdaBuilder.build();
            }catch (LoginException e){
                System.err.println("Connection failed");
                e.printStackTrace();
                return false;
            }

            jdaInterface.awaitReady();
            LAUNCH_TIME = System.currentTimeMillis();
            servers = new Servers();
            channels = new Channels(servers);
            actions = new Actions(channels);
            //permissions = new util.Permissions();
            BOT_ID = jdaInterface.getSelfUser().getIdLong();
            jdaInterface.addEventListener(new EventsListener());
        }
        return true;
    }

    private static void loadConfig(){
        FileSeeker fileSeeker = new FileSeeker("config.json");
        String configPath = fileSeeker.findTargetPath();
        if(configPath.isEmpty()){
            return;
        }
        JSONObject data = parseJSON(configPath);
        try{
            TOKEN = data.getString("token");
            String prefix = data.getString("prefix");
            Bot.PREFIX = prefix == null ? Bot.PREFIX : prefix;
            Bot.PREFIX_OFFSET = Bot.PREFIX.length();
            Iterator<Object> sudoers = data.getJSONArray("sudo_users").iterator();
            while (sudoers.hasNext()){
                long userId = (Long) sudoers.next();
                Bot.AUTHORIZED_USERS.add(userId);
            }
            MessageProcessor.PURGE_CAP = data.getInt("purge_cap");
            MessageProcessor.MAX_DEQUE_SIZE = data.getInt("message_cap_per_channel");
            String audioDir = data.getString("audio_dir");
            if(!Files.isDirectory(Paths.get(audioDir))){
                System.err.println("Audio directory doesn't exist");
            }else{
                AudioPlayer.AUDIO_FILES_DIR = new File(audioDir);
            }
        }catch(JSONException jsonExc){
            System.err.println("JSON config error");
        }
    }

    private static JSONObject parseJSON(final String PATH){
        BufferedReader reader;
        JSONObject jsonMap = null;
        try{
            reader = new BufferedReader(new FileReader(PATH));

            StringBuilder contents = new StringBuilder();
            String line;
            while((line = reader.readLine())!= null){
                contents.append(line);
            }
            jsonMap = new JSONObject(contents.toString());
        }catch (IOException e){
            e.printStackTrace();
        }
        return jsonMap;
    }
    public static JDA getJDAInterface(){
        return jdaInterface;
    }
    public static Actions getActions(){
        return actions;
    }
    public static Servers getServers(){
        return servers;
    }
    public static Channels getChannels(){
        return channels;
    }
    public static long getUptime(){
        return System.currentTimeMillis() - LAUNCH_TIME;
    }
}
