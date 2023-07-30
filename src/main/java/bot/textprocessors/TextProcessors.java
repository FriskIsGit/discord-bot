package bot.textprocessors;

import bot.core.Bot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public final class TextProcessors{
    private static TextProcessors instance;

    private final TextProcessor[] textProcessors = new TextProcessor[]{
            new BallsDex()
    };
    private TextProcessors(){
    }

    public static TextProcessors get(){
        if(instance == null){
            instance = new TextProcessors();
        }
        return instance;
    }

    public void passMessage(Message message, boolean isEdit){
        String content = message.getContentRaw();
        if(content.startsWith(Bot.PREFIX)){
            return;
        }
        for(TextProcessor processor : textProcessors){
            if(processor.consume(content, message, isEdit)){
                break;
            }
        }
    }
}

abstract class TextProcessor{
    // if message is consumed it will not be passed to other text processors
    abstract boolean consume(String content, Message message, boolean isEdit);
}
