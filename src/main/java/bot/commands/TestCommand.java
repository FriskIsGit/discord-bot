package bot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TestCommand extends Command {
    public TestCommand(String... aliases) {
        super(aliases);
        description = "Placeholder for any command";
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {

    }
}
