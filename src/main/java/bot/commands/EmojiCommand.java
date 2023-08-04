package bot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EmojiCommand extends Command{
    private static final String DISCORD_CONTENT_URL = "https://cdn.discordapp.com/emojis/{id}.webp?size={size}&quality=lossless";
    // default emoji <:name:id>
    public EmojiCommand(String... aliases){
        super(aliases);
        description = "Fetches underlying emoji link\n" +
                    "Optional parameter: size (44, 128)";
        usage = "emoji :emoji:\n" +
                "emoji :emoji: `size`";
    }
    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 0){
            return;
        }
        if(message == null){
            return;
        }
        String emojiContent = args[0];
        if(emojiContent.length() < 3){
            return;
        }
        int colon = emojiContent.indexOf(':', 3);
        if(colon == -1 || colon + 1 >= emojiContent.length()){
            return;
        }
        int bracket = emojiContent.indexOf('>', colon + 1);
        if(bracket == -1){
            return;
        }
        String id = emojiContent.substring(colon + 1, bracket);
        String url = DISCORD_CONTENT_URL.replace("{id}", id);
        if(args.length > 1){
            url = url.replace("{size}", args[1]);
        }else{
            url = url.replace("{size}", "");
        }
        actions.sendAsMessageBlock(message.getChannel(), url);
    }
}
