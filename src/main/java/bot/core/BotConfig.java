package bot.core;

import bot.utilities.FileSeeker;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BotConfig{
    public boolean exists;
    public String token, prefix, openAIToken, ai21Token, geniusToken;
    public int purgeCap = 500, maxDequeSize = 1000;
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

    public static BotConfig readConfig(){
        FileSeeker fileSeeker = new FileSeeker("config.json");
        String configPath = fileSeeker.findTargetPath();
        if(configPath.isEmpty()){
            return BotConfig.notExists();
        }
        JSONObject data = parseJSON(configPath);
        BotConfig config = new BotConfig();
        try{
            config.token = data.getString("token");
            config.prefix = data.getString("prefix");
            Bot.AUTHORIZED_USERS.clear();
            for (Object sudo : data.getJSONArray("sudo_users")){
                long userId = (Long) sudo;
                Bot.AUTHORIZED_USERS.add(userId);
            }
            JSONObject tokens = data.getJSONObject("tokens");
            config.openAIToken = tokens.getString("open_ai_token");
            config.ai21Token = tokens.getString("ai21_token");
            config.geniusToken = tokens.getString("genius_token");

            config.purgeCap = data.getInt("purge_cap");
            config.maxDequeSize = data.getInt("message_cap_per_channel");
            String audioDir = data.getString("audio_dir");
            if(!Files.isDirectory(Paths.get(audioDir))){
                System.err.println("Audio directory doesn't exist");
            }else{
                config.audioDirectory = new File(audioDir);
            }
        }catch(JSONException e){
            System.err.println(e.getMessage());
        }
        return config;
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
            e.printStackTrace();
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
