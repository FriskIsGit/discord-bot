package bot.core;

import bot.core.emergency.EmergencyListener;
import bot.utilities.jda.ShutdownTimer;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import bot.utilities.jda.Actions;
import no4j.core.Logger;
import no4j.core.No4JConfiguration;

import java.io.IOException;
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
    private static BotConfig config;
    private static ShutdownTimer shutdownTimer;

    public static void initialize(String configPath) throws IOException, InterruptedException {
        No4JConfiguration.configure();
        adjustLoggers();
        Logger log = Logger.getLogger("primary");

        JDABuilder jdaBuilder;
        config = BotConfig.readConfig(configPath);
        if(!config.exists){
            log.error("Config file not found.");
        }
        Bot.PREFIX = config.prefix == null ? Bot.PREFIX : config.prefix;
        Bot.PREFIX_OFFSET = Bot.PREFIX.length();

        if(!config.hasToken()){
            log.fatal("Token is null, exiting..");
            System.exit(0);
        }
        jdaBuilder = JDABuilder.createDefault(config.token);
        if(config.enableEmergency){
            jdaBuilder.addEventListeners(new EmergencyListener(jdaBuilder));
        }
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
    }

    private static void adjustLoggers() {
        Logger primaryLog = Logger.getLogger("primary");
        primaryLog.getConsole().enableColor(true);
        primaryLog.getConfig().setMethodPadLength(50);

        Logger chatLog = Logger.getLogger("chat");
        chatLog.getConfig().setMethodPadLength(0);
        chatLog.getConfig().setLevelPadLength(0);
        chatLog.getConfig().includeMethod(false);
        chatLog.getConsole().enableColor(true);

        Logger eventLog = Logger.getLogger("events");
        eventLog.getConfig().setLevelPadLength(0);
        eventLog.getConfig().setMethodPadLength(0);
        eventLog.getConfig().includeMethod(false);
        eventLog.getConsole().enableColor(true);
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
    public static ShutdownTimer getShutdownTimer(){
        return shutdownTimer;
    }
    public static long getUptime(){
        return System.currentTimeMillis() - LAUNCH_TIME;
    }
}
