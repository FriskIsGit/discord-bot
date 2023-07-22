package bot.textprocessors;

import bot.core.Bot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public final class TextProcessors{
    private static TextProcessors instance;

    private final TextProcessor[] textProcessors = new TextProcessor[]{
            // new BallsDex()
    };
    private TextProcessors(){
    }

    public static TextProcessors get(){
        if(instance == null){
            instance = new TextProcessors();
        }
        return instance;
    }

    public void passMessage(MessageReceivedEvent message){
        String content = message.getMessage().getContentRaw();
        if(content.startsWith(Bot.PREFIX)){
            return;
        }
        for(TextProcessor processor : textProcessors){
            if(processor.consume(content, message)){
                break;
            }
        }
    }
}

abstract class TextProcessor{
    // if message is consumed it will not be passed to other text processors
    abstract boolean consume(String content, MessageReceivedEvent message);
}
