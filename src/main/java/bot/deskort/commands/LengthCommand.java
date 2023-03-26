package bot.deskort.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LengthCommand extends Command{
    public LengthCommand(String... aliases){
        super(aliases);
        description = "Returns text length";
        usage = "len `text`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length < 1){
            return;
        }
        actions.messageChannel(message.getChannel(), "``" + args[0].length() +"``");
    }
}
