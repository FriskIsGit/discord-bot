package bot.utilities;

import bot.deskort.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.Locale;

public class Channels{
    private final JDA jdaInterface;
    private final Servers servers;

    public Channels(Servers servers){
        jdaInterface = Bot.getJDAInterface();
        this.servers = servers;
    }

    public TextChannel getTextChannel(String partialName){
        List<TextChannel> textChannels = jdaInterface.getTextChannels();
        for(int i = 0; i<textChannels.size(); i++){
            if(textChannels.get(i).getName().contains(partialName)){
                return textChannels.get(i);
            }
        }
        return null;
    }
    public TextChannel getTextChannel(long id){
        return jdaInterface.getTextChannelById(id);
    }
    public VoiceChannel getVoiceChannel(long id){
        return jdaInterface.getVoiceChannelById(id);
    }

    public VoiceChannel getVoiceChannel(String partialName){
        List<VoiceChannel> voiceChannels = jdaInterface.getVoiceChannels();
        for (VoiceChannel voiceChannel : voiceChannels){
            String voiceName = voiceChannel.getName();
            if (voiceName.contains(partialName)){
                return voiceChannel;
            }
        }
        return null;
    }

    public VoiceChannel getVoiceChannelIgnoreCase(String partialName){
        String lowerCaseName = partialName.toLowerCase(Locale.ROOT);
        List<VoiceChannel> voiceChannels = jdaInterface.getVoiceChannels();
        for (VoiceChannel voiceChannel : voiceChannels){
            String voiceName = voiceChannel.getName().toLowerCase(Locale.ROOT);
            if (voiceName.contains(lowerCaseName)){
                return voiceChannel;
            }
        }
        return null;
    }

    public Channel getChannel(String partialName){
        List<Guild> serversList = jdaInterface.getGuilds();
        for(Guild guild : serversList){
            List<GuildChannel> listOfChannels = guild.getChannels();
            for(GuildChannel channel : listOfChannels){
                if(channel.getName().contains(partialName)){
                    return channel;
                }
            }
        }
        return null;
    }

}
