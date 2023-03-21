package bot.utilities;

import bot.deskort.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

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
        for (TextChannel textChannel : textChannels){
            if (textChannel.getName().contains(partialName)){
                return textChannel;
            }
        }
        return null;
    }
    public MessageChannel getMessageChannel(String partialName){
        List<Guild> serversList = jdaInterface.getGuilds();
        for(Guild guild : serversList){
            List<GuildChannel> listOfChannels = guild.getChannels();
            for(GuildChannel channel : listOfChannels){
                if(channel.getName().contains(partialName)){
                    if(channel instanceof MessageChannel){
                        return (MessageChannel) channel;
                    }
                }
            }
        }
        return null;
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

}
