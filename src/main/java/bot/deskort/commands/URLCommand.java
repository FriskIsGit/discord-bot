package bot.deskort.commands;

import bot.deskort.MessageProcessor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class URLCommand extends Command{
    private final MessageProcessor messageProcessor;
    public URLCommand(String... aliases){
        super(aliases);
        description = "Responds with the underlying attachment link, then deletes the message";
        usage = "link";
        messageProcessor = MessageProcessor.get();
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        Message msg = message.getMessage();
        List<Message.Attachment> attachments = msg.getAttachments();
        StringBuilder links = new StringBuilder();
        for(Message.Attachment attachment : attachments){
            links.append(attachment.getUrl());
        }
        messageProcessor.deleteRequestMessage(msg);
        actions.sendAsMessageBlock(message.getChannel(), links.toString());
    }
}
