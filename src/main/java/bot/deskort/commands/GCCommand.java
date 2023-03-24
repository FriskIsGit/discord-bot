package bot.deskort.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GCCommand extends Command{
    //gc mem song zzz sleep
    public GCCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        System.gc();
    }
}
