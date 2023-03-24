package bot.deskort.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class FlushSongsCommand extends Command{
    public FlushSongsCommand(String[] aliases){
        super(aliases);
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){

    }
}
