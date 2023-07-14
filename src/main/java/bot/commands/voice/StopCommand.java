package bot.commands.voice;

import bot.commands.Command;
import bot.music.AudioPlayer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class StopCommand extends Command{
    public StopCommand(String... aliases){
        super(aliases);
        description = "Stops playback of the current song";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        AudioPlayer audioPlayer = (AudioPlayer) audioManager.getSendingHandler();
        if(audioPlayer !=  null){
            audioPlayer.setPlaying(false);
        }
    }
}
