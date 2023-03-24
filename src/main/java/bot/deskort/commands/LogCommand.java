package bot.deskort.commands;

import bot.deskort.MessageDeque;
import bot.deskort.MessageProcessor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LogCommand extends Command{
    public LogCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
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
