package bot.deskort.commands;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EmojiCommand extends Command{
    public EmojiCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        MessageChannel channel = message.getChannel();
        channel.sendMessage(Commands.mergeTerms(args)).queue();
    }
}
