package bot.deskort.commands;

import bot.utilities.Hasher;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HashCommand extends Command{

    public HashCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length < 1){
            actions.messageChannel(message.getChannel(), "Nothing to hash");
            return;
        }
        String merged = Commands.mergeTerms(args);
        String hash = Hasher.hash(merged, Hasher.choose(commandName));
        actions.messageChannel(message.getChannel(), hash);
    }
}
