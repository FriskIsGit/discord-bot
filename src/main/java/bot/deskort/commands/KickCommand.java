package bot.deskort.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class KickCommand extends Command{
    public KickCommand(String... aliases){
        super(aliases);
        usage = "Kick `user_id` `reason`";
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        actions.messageChannel(message.getChannel(), "Not implemented");
    }
}
