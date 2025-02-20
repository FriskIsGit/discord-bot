package bot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

// This could be expanded to give more control over what is logged and at what level
public class LogCommand extends Command {
    public LogCommand(String... aliases) {
        super(aliases);
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
    }
}
