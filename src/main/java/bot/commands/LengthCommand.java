package bot.commands;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LengthCommand extends Command{
    private static final boolean FALLBACK_CHANNEL = true;
    public LengthCommand(String... aliases){
        super(aliases);
        description = "Returns text length";
        usage = "len `text`";
        triggerableByBot = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length < 1){
            return;
        }
        int len = args[0].length();
        if(message == null){
            if(!FALLBACK_CHANNEL){
                return;
            }
            TextChannel channel = actions.getTextChannel("spam");
            if(channel == null){
                return;
            }
            actions.messageChannel(channel, "``" + len +"``");
            return;
        }
        actions.messageChannel(message.getChannel(), "``" + len +"``");
    }
}
