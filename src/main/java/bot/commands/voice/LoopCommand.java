package bot.commands.voice;

import bot.commands.Command;
import bot.music.AudioPlayer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class LoopCommand extends Command{
    public LoopCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        AudioPlayer audioPlayer = AudioPlayer.addSendingHandlerIfNull(audioManager);
        boolean isLooping = audioPlayer.switchLooping();
        actions.messageChannel(message.getChannel(), "**Looping set to " + isLooping + "**");
    }
}
