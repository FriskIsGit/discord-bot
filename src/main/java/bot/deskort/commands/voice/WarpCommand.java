package bot.deskort.commands.voice;

import bot.deskort.commands.Command;
import bot.deskort.commands.Commands;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class WarpCommand extends Command{
    public WarpCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        Member msgAuthor = message.getMember();
        GuildVoiceState vcState = Objects.requireNonNull(msgAuthor).getVoiceState();
        if(vcState != null && !vcState.inAudioChannel()){
            return;
        }
        Guild thisGuild = message.getGuild();

        List<VoiceChannel> voiceChannels = thisGuild.getVoiceChannels();
        String channelNameLower = Commands.mergeTerms(args).toLowerCase(Locale.ROOT);
        VoiceChannel destinationChannel = null;
        for (VoiceChannel vc : voiceChannels){
            if(vc.getName().toLowerCase().contains(channelNameLower)){
                destinationChannel = vc;
                break;
            }
        }
        if(destinationChannel == null){
            return;
        }
        thisGuild.moveVoiceMember(msgAuthor, destinationChannel).queue();
    }
}
