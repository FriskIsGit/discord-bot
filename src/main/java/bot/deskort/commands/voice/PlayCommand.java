package bot.deskort.commands.voice;

import bot.deskort.commands.Command;
import bot.deskort.commands.Commands;
import bot.music.AudioPlayer;
import bot.music.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class PlayCommand extends Command{
    public PlayCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        if(!audioManager.isConnected()){
            Commands.get().command("join").execute(null, message);
        }
        String mergedArgs = Commands.mergeTerms(args);

        AudioPlayer audioPlayer = AudioPlayer.addSendingHandlerIfNull(audioManager);
        if(!mergedArgs.isEmpty()){
            if (!audioPlayer.setAudioTrack(mergedArgs)){
                actions.messageChannel(message.getChannel(), "Track doesn't exist");
                return;
            }
        }else if(audioPlayer.getCurrentAudioTrack() == null){
            //play from playlist
            String nextSong = audioPlayer.getSongQueue().take();
            audioPlayer.setAudioTrack(nextSong);
        }
        actions.sendEmbed(message.getChannel(), createPlayingEmbed(audioPlayer));
        audioPlayer.setPlaying(true);
    }

    private static MessageEmbed createPlayingEmbed(AudioPlayer audioPlayer){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        AudioTrack currentAudioTrack = audioPlayer.getCurrentAudioTrack();
        if(currentAudioTrack != null){
            embedBuilder.setTitle("Now playing");
            embedBuilder.setDescription(currentAudioTrack.getTrackName());
            int lengthSeconds = (int)(currentAudioTrack.getLengthSeconds());
            String seconds = String.valueOf(lengthSeconds);
            embedBuilder.appendDescription("\nDuration: ").appendDescription(seconds).appendDescription("s");
            return embedBuilder.build();
        }
        embedBuilder.setTitle("Nothing is playing right now");
        embedBuilder.setDescription("**sleepy noises**");
        return embedBuilder.build();
    }
}
