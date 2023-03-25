package bot.deskort.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SayCommand extends Command{

    public SayCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 0){
            return;
        }
        String merge = Commands.mergeTerms(args);
        if(merge.length() > 2000){
            return;
        }
        actions.messageChannel(message.getChannel(), merge);
    }
}
