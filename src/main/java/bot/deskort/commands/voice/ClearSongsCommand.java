package bot.deskort.commands.voice;

import bot.deskort.commands.Command;
import bot.music.AudioPlayer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ClearSongsCommand extends Command{
    public ClearSongsCommand(String... aliases){
        super(aliases);
        description = "Removes loaded tracks from memory";
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioPlayer.clearAudioTracksFromMemory();
    }
}
