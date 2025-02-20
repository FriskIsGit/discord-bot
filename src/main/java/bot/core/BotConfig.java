package bot.core;

import bot.utilities.FileSeeker;
import no4j.core.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BotConfig{
    private static final Logger logger = Logger.getLogger("primary");

    public boolean exists;
    public String token, prefix, openAIToken, ai21Token, geniusToken;
    public int purgeCap, maxDequeSize;
    public boolean enableEmergency, enableShell;
    public File audioDirectory;

    public BotConfig(){
        exists = true;
    }

    public boolean hasToken(){
        return token != null;
    }

    public static BotConfig notExists(){
        BotConfig config = new BotConfig();
        config.exists = false;
        return config;
    }

    public static BotConfig readConfig(String configPath){
        if(configPath == null || !isFile(configPath)){
            FileSeeker fileSeeker = new FileSeeker("config.json");
            configPath = fileSeeker.findTargetPath();
        }

        if(configPath.isEmpty()){
            return BotConfig.notExists();
        }
        JSONObject data = parseJSON(configPath);
        BotConfig config = new BotConfig();
        try{
            config.token = data.getString("token");
            config.prefix = data.optString("prefix", ">");

            if(data.has("sudo_users")){
                Bot.AUTHORIZED_USERS.clear();
                for (Object sudo : data.getJSONArray("sudo_users")){
                    long userId = (Long) sudo;
                    Bot.AUTHORIZED_USERS.add(userId);
                }
            }

            if(data.has("tokens")){
                JSONObject tokens = data.getJSONObject("tokens");
                config.openAIToken = tokens.optString("open_ai_token");
                config.ai21Token = tokens.optString("ai21_token");
                config.geniusToken = tokens.optString("genius_token");
            }

            config.purgeCap = data.optInt("purge_cap", 500);
            config.maxDequeSize = data.optInt("message_cap_per_channel", 1000);
            String audioDir = data.optString("audio_dir");
            if(!Files.isDirectory(Paths.get(audioDir))){
                logger.error("Audio directory doesn't exist");
            }else{
                config.audioDirectory = new File(audioDir);
            }
            config.enableEmergency = data.optBoolean("enable_emergency", true);
            config.enableShell = data.optBoolean("enable_shell", false);
        }catch(JSONException e){
            logger.exception(e);
        }
        return config;
    }

    private static boolean isFile(String path){
        try{
            Path p = Paths.get(path);
            return Files.isRegularFile(p);
        }catch (InvalidPathException e){
            return false;
        }
    }

    private static JSONObject parseJSON(final String PATH){
        BufferedReader reader = null;
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
            logger.stackTrace("", e);
        }finally{
            closeSilently(reader);
        }
        return jsonMap;
    }

    private static void closeSilently(BufferedReader reader){
        if(reader == null){
            return;
        }
        try{
            reader.close();
        }catch (IOException ignored){
        }
    }
}
