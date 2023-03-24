package bot.deskort.commands.voice;

import bot.deskort.commands.Command;
import bot.music.AudioPlayer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class StopCommand extends Command{
    public StopCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        AudioPlayer audioPlayer = (AudioPlayer) audioManager.getSendingHandler();
        if(audioPlayer ==  null){
            audioPlayer = new AudioPlayer();
            audioManager.setSendingHandler(audioPlayer);
            return;
        }
        audioPlayer.setPlaying(false);
    }
}
