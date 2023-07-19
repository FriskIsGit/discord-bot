package bot.commands;

import bot.core.Bot;
import bot.core.BotConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class ConfigCommand extends Command{
    private static final Color niceGreen = new Color(34,139,34);
    private static final Color crimson = new Color(220,20,60);
    public ConfigCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
        description = "Reloads config from disk";
        usage = "config reload";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 0){
            return;
        }
        if(!args[0].equals("reload")){
            return;
        }
        BotConfig config = BotConfig.readConfig();
        MessageChannelUnion channel = message.getChannel();
        if(!config.exists){
            actions.sendEmbed(channel, embed("Config file not found", false));
            return;
        }
        Bot.PREFIX = config.prefix == null ? Bot.PREFIX : config.prefix;
        Bot.PREFIX_OFFSET = Bot.PREFIX.length();
        Bot.setConfig(config);
        actions.sendEmbed(channel, embed("Config loaded", true));
    }
    private static MessageEmbed embed(String content, boolean positive){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(positive ? "Success" : "Error");
        embed.setDescription(content);
        embed.setColor(positive ? niceGreen : crimson);
        return embed.build();
    }
}
