package bot.commands.voice;

import bot.commands.Command;
import bot.commands.Commands;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class WarpCommand extends Command{
    private final boolean complyWithJoinPermission = true;
    public WarpCommand(String... aliases){
        super(aliases);
        description = "Moves you to another channel";
        usage = "warp `channel`";
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

        if(!isAuthorized(msgAuthor.getIdLong()) && complyWithJoinPermission){
            List<PermissionOverride> memberOverrides = destinationChannel.getPermissionContainer().getMemberPermissionOverrides();
            for(PermissionOverride perm : memberOverrides){
                if(perm.getMember() == null || perm.getMember().getIdLong() != msgAuthor.getIdLong()){
                    continue;
                }
                if(perm.getDeniedRaw() == Permission.VOICE_CONNECT.getRawValue()){
                    actions.sendAsMessageBlock(message.getChannel(), "Missing VOICE_CONNECT permission");
                    return;
                }
            }
        }

        thisGuild.moveVoiceMember(msgAuthor, destinationChannel).queue();
    }
}
