package bot.commands.voice;

import bot.commands.Command;
import bot.music.AudioPlayer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class SkipCommand extends Command{
    public SkipCommand(String... aliases){
        super(aliases);
        description = "Consumes the first element from song queue and attempts to play it";
        usage = "skip";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        AudioPlayer audioPlayer = AudioPlayer.addSendingHandlerIfNull(audioManager);
        audioPlayer.setAudioTrack(audioPlayer.getSongQueue().take());
    }
}
