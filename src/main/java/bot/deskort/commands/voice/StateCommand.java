package bot.deskort.commands.voice;

import bot.deskort.commands.Command;
import bot.utilities.NotNull;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class StateCommand extends Command{

    public StateCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audio = message.getGuild().getAudioManager();

        if(audio.isConnected()){
            actions.messageChannel(
                    message.getChannel(),
                    "TRUE, bot is currently connected to: " + NotNull.notNull(audio.getConnectedChannel()).getName()
            );
            return;
        }
        actions.messageChannel(message.getChannel(), "FALSE, bot is not connected to any voice channel");
    }
}
