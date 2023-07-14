package bot.commands.voice;

import bot.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class LeaveCommand extends Command{
    public LeaveCommand(String... aliases){
        super(aliases);
        description = "Disconnects bot from channel";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        audioManager.closeAudioConnection();
    }
}
