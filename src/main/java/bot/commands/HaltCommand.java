package bot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HaltCommand extends Command {

    public HaltCommand(String... aliases) {
        super(aliases);
        requiresAuth = true;
        usage = "halt";
        description = "Cancels pending message requests to stop bot from spamming";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
        System.out.println("IT WAS HALTED");
        actions.clearQueuedMessages();
    }
}
