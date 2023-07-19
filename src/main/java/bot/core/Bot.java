package bot.core;

import bot.core.emergency.EmergencyListener;
import bot.utilities.jda.ShutdownTimer;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import bot.utilities.jda.Actions;

import java.util.HashSet;
import java.util.Set;

public class Bot{

    private Bot(){}
    public static long BOT_ID;
    public static final Set<Long> AUTHORIZED_USERS = new HashSet<>();
    public static String PREFIX = ">";
    public static int PREFIX_OFFSET = 1;

    private static long LAUNCH_TIME = 0;
    private static JDA jdaInterface;
    private static Actions actions;
    private static MessageProcessor messageProcessor;
    private static BotConfig config;
    private static ShutdownTimer shutdownTimer;
    //private static bot.utilities.jda.Permissions permissions;

    public static void initialize() throws InterruptedException{
        JDABuilder jdaBuilder;
        config = BotConfig.readConfig();
        if(!config.exists){
            System.out.println("Config file not found.");
        }
        Bot.PREFIX = config.prefix == null ? Bot.PREFIX : config.prefix;
        Bot.PREFIX_OFFSET = Bot.PREFIX.length();

        if(!config.hasToken()){
            System.out.println("Token is null, exiting..");
            System.exit(0);
        }
        jdaBuilder = JDABuilder.createDefault(config.token);
        jdaBuilder.addEventListeners(new EmergencyListener(jdaBuilder));
        //voice limits
        jdaBuilder.enableCache(CacheFlag.VOICE_STATE);
        jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        //jdaBuilder.disableCache(CacheFlag.MEMBER_OVERRIDES);

        jdaInterface = jdaBuilder.build();

        jdaInterface.awaitReady();
        LAUNCH_TIME = System.currentTimeMillis();
        actions = new Actions();
        shutdownTimer = new ShutdownTimer();

        BOT_ID = jdaInterface.getSelfUser().getIdLong();
        jdaInterface.addEventListener(new EventsListener());
        messageProcessor = MessageProcessor.get();
    }

    public static JDA getJDAInterface(){
        return jdaInterface;
    }
    public static Actions getActions(){
        return actions;
    }
    public static BotConfig getConfig(){
        return config;
    }
    public static void setConfig(BotConfig newConfig){
        config = newConfig;
    }
    public static MessageProcessor getMessageProcessor(){
        return messageProcessor;
    }
    public static ShutdownTimer getShutdownTimer(){
        return shutdownTimer;
    }
    public static long getUptime(){
        return System.currentTimeMillis() - LAUNCH_TIME;
    }
}
