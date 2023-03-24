package bot.deskort.commands.voice;

import bot.deskort.commands.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;

public class RegainCommand extends Command{
    public RegainCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        Guild server = message.getGuild();
        AudioManager audioManager = server.getAudioManager();
        AudioChannel currentAudioChannel = audioManager.getConnectedChannel();
        if(currentAudioChannel == null){
            actions.messageChannel(message.getChannel(), "Not connected");
            return;
        }
        List<VoiceChannel> audioChannels = server.getVoiceChannels();
        if(audioChannels.size() < 2){
            actions.messageChannel(message.getChannel(), "Not enough channels");
            return;
        }
        VoiceChannel swapChannel = null;
        for(VoiceChannel vc : audioChannels){
            if(vc.getIdLong() != currentAudioChannel.getIdLong()){
                swapChannel = vc;
                break;
            }
        }
        Member botMember = server.getMember(jda.getSelfUser());
        if(botMember == null){
            return;
        }
        server.moveVoiceMember(botMember, swapChannel).complete();
        server.moveVoiceMember(botMember, currentAudioChannel).complete();
    }
}
