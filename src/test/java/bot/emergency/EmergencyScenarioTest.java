package bot.emergency;

import bot.core.Bot;
import bot.utilities.jda.Actions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class EmergencyScenarioTest{
    private static JDA jdaInterface;
    private static Guild guild;
    private static Actions actions;
    public static long guildId = 519857141494841344L;

    public static final String[] channelNames = {
            "test_channel_1",
            "test_channel_2",
            "test_channel_3",
            "test_channel_4",
            "test_channel_5"
    };
    public static final List<TextChannel> textChannels = new ArrayList<>(channelNames.length);

    public static void initAndCreateChannels(){
        try{
            Bot.initialize();
        }catch (InterruptedException e){
            e.printStackTrace();
            return;
        }
        jdaInterface = Bot.getJDAInterface();
        actions = Bot.getActions();
        guild = jdaInterface.getGuildById(guildId);
        for(String channelName : channelNames){
            Objects.requireNonNull(guild).createTextChannel(channelName).queue();
        }

    }
    public static void getAndDeleteChannels(){
        for(String channelName : channelNames){
            TextChannel channel = actions.getTextChannel(channelName);
            if(channel != null){
                textChannels.add(channel);
            }
        }
        for(TextChannel textChannel : textChannels){
            textChannel.delete().queue();
        }

    }
    public static void banAndKickSimultaneously(long id1, long id2, long id3){
        jdaInterface = Bot.getJDAInterface();
        guild = jdaInterface.getGuildById(guildId);

        User user1 = jdaInterface.retrieveUserById(id1).complete();
        User user2 = jdaInterface.retrieveUserById(id2).complete();
        User user3 = jdaInterface.retrieveUserById(id3).complete();
        if(user1 != null && user2 != null && user3 != null){
            new Thread(() -> {
                guild.ban(user1, 0, TimeUnit.MICROSECONDS).reason("Test ban1").complete();
            }).start();
            try{
                Thread.sleep(250);
            }catch (InterruptedException interruptedException){
                interruptedException.printStackTrace();
            }
            new Thread(() -> {
                guild.kick(user2).reason("Test kick2").complete();
            }).start();
            new Thread(() -> {
                guild.ban(user3, 0, TimeUnit.MICROSECONDS).reason("Test ban3").complete();
            }).start();
        }
    }
    @Test
    public void run(){
    }
}
