package bot.deskort.commands;

import bot.deskort.Bot;
import bot.utilities.jda.ShutdownTimer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AbortCommand extends Command{
    public AbortCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
        description = "Aborts shutdown";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        ShutdownTimer shutdownTimer = Bot.getShutdownTimer();
        if(!shutdownTimer.isScheduled()){
            return;
        }
        shutdownTimer.abort();
        actions.sendAsMessageBlock(message.getChannel(), "Aborting shutdown");
    }
}
