package bot.deskort.commands;

import bot.deskort.Bot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UptimeCommand extends Command{
    public UptimeCommand(String... aliases){
        super(aliases);
        description = "Uptime";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        int uptimeSeconds = (int) (Bot.getUptime() / 1000);
        int hours = uptimeSeconds / 3600;
        int min = (uptimeSeconds/60) % 60;
        int sec = uptimeSeconds % 60;
        //this should be converted to a StringBuilder by the compiler
        String response = "Uptime: " + hours + "h " +
                min + "m " +
                sec + 's';
        actions.sendAsMessageBlock(message.getChannel(), response);
    }
}
