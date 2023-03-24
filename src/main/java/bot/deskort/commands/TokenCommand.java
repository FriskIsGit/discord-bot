package bot.deskort.commands;

import bot.utilities.PseudoBotTokenGenerator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TokenCommand extends Command{
    public TokenCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        String token = PseudoBotTokenGenerator.generateBotToken();
        actions.messageChannel(message.getChannel(), token);
    }
}
