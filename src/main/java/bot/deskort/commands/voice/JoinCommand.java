package bot.deskort.commands.voice;

import bot.deskort.commands.Command;
import bot.deskort.commands.Commands;
import bot.music.AudioPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;
import java.util.Locale;

public class JoinCommand extends Command{
    private static final boolean allowCrossGuildJoins = true;
    public JoinCommand(String... aliases){
        super(aliases);
        description = "Joins channel which user is in or channel with specified name\n" +
                "Example use case: join `partial_name`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        AudioPlayer.addSendingHandlerIfNull(audioManager);

        if(args.length < 1){
            //join channel that user is in
            Member member = message.getMember();
            if(member == null)
                return;
            GuildVoiceState membersVoiceState = member.getVoiceState();
            if(membersVoiceState == null){
                System.out.println("Null voice state");
                return;
            }
            if(membersVoiceState.inAudioChannel()){
                //VoiceChannel is also fine
                AudioChannel voice = member.getVoiceState().getChannel();
                audioManager.openAudioConnection(voice);
            }else{
                actions.messageChannel(message.getChannel(), "Member not in voice");
            }
            return;
        }
        //channel specific join
        String mergedArgs = Commands.mergeTerms(args);
        VoiceChannel voice;
        if(allowCrossGuildJoins){
            voice = getAnyVoiceChannelIgnoreCase(mergedArgs);
            if(voice == null){
                actions.messageChannel(message.getChannel(), "No voice channel matched");
                actions.messageChannel(message.getChannel(), mergedArgs);
                return;
            }
            //if targeting another server
            if(message.getGuild().getIdLong() != voice.getGuild().getIdLong()){
                audioManager = voice.getGuild().getAudioManager();
                AudioPlayer.addSendingHandlerIfNull(audioManager);
            }
        }else{
            voice = getVoiceChannelIgnoreCase(mergedArgs, message.getGuild());
            if(voice == null){
                actions.messageChannel(message.getChannel(), "No voice channel matched");
                return;
            }
        }
        audioManager.openAudioConnection(voice);
    }
    private VoiceChannel getAnyVoiceChannelIgnoreCase(String partialName){
        String lowerCaseName = partialName.toLowerCase(Locale.ROOT);
        List<VoiceChannel> voiceChannels = jda.getVoiceChannels();
        for (VoiceChannel voiceChannel : voiceChannels){
            String voiceName = voiceChannel.getName().toLowerCase(Locale.ROOT);
            if (voiceName.contains(lowerCaseName)){
                return voiceChannel;
            }
        }
        return null;
    }
    private VoiceChannel getVoiceChannelIgnoreCase(String partialName, Guild guild){
        String lowerCaseName = partialName.toLowerCase(Locale.ROOT);
        List<VoiceChannel> voiceChannels = guild.getVoiceChannels();
        for (VoiceChannel voiceChannel : voiceChannels){
            String voiceName = voiceChannel.getName().toLowerCase(Locale.ROOT);
            if (voiceName.contains(lowerCaseName)){
                return voiceChannel;
            }
        }
        return null;
    }
}
