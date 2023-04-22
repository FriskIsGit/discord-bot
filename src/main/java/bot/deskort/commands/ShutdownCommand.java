package bot.deskort.commands;

import bot.deskort.Bot;
import bot.utilities.jda.ShutdownTimer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ShutdownCommand extends Command{
    public ShutdownCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
        description = "Schedules a shutdown after a specified time period\n" +
                      "Acceptable units: hours, minutes, seconds, days.";
        usage = "shutdown `number unit`\n" +
                "shutdown 1h";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        ShutdownTimer shutdownTimer = Bot.getShutdownTimer();
        if(args.length > 0){
            int seconds = ShutdownTimer.parseToSeconds(Commands.mergeTerms(args));
            if(seconds == -1){
                actions.sendAsMessageBlock(message.getChannel(), "Shutdown argument failure");
                return;
            }
            shutdownTimer.countdown(seconds);
        }else{
            shutdownTimer.countdown(0);
        }
        actions.sendAsMessageBlock(message.getChannel(), "Shutdown scheduled");
    }
}
