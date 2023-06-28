package bot.deskort.commands;

import bot.utilities.jda.MessageDeque;
import bot.deskort.MessageProcessor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LogCommand extends Command{
    public LogCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(message == null){
            TextChannel channel = actions.getTextChannel(String.join(" ", args));
            if(channel == null){
                return;
            }
            MessageDeque deq = MessageProcessor.get().channelIdsToMessageDeques.get(channel.getIdLong());
            if(deq != null){
                deq.print();
            }
            return;
        }
        Message theMessage = message.getMessage();
        theMessage.delete().queue();
        long channelId = theMessage.getChannel().getIdLong();
        MessageDeque deq = MessageProcessor.get().channelIdsToMessageDeques.get(channelId);
        if(deq != null){
            deq.removeLast();
            deq.print();
        }
    }
}
