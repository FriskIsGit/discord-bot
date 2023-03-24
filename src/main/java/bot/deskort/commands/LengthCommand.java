package bot.deskort.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LengthCommand extends Command{
    public LengthCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        String argsMerged = Commands.mergeTerms(args);
        actions.messageChannel(message.getChannel(), "``" + argsMerged.length() +"``");
    }
}
