package bot.deskort.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CatCommand extends Command{
    public CatCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        List<Message.Attachment> attachments = message.getMessage().getAttachments();
        if(attachments.size() == 0){
            System.out.println("No file attached to cat");
        }
    }
}
