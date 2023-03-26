package bot.deskort.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends Command{
    private static final Color niceGreen = new Color(34,139,34);
    public HelpCommand(String... aliases){
        super(aliases);
        description = "Prints information about commands or a command";
        usage = "help `command_alias`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 0){
            actions.sendEmbed(message.getChannel(), createEmbedForEnabledCommands());
            return;
        }
        Command command = Commands.get().command(args[0]);
        if(command == null){
            actions.messageChannel(message.getChannel(), "`" + args[0] + "` doesn't map to any command");
            return;
        }
        actions.sendEmbed(message.getChannel(), createEmbedForCommand(command));
    }

    private MessageEmbed createEmbedForCommand(Command command){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(niceGreen);
        String aliases = formatAliases(command.aliases).toString();
        String desc = command.description;
        String usage = command.usage;
        String enabled = String.valueOf(command.enabled);
        String requiresAuth = String.valueOf(command.requiresAuth);

        embedBuilder.addField("Aliases", aliases,false);
        if(!desc.isEmpty()){
            embedBuilder.addField("Description", desc,false);
        }
        if(!usage.isEmpty()){
            embedBuilder.addField("Usage", usage,false);
        }
        embedBuilder.addField("Enabled", enabled,true);
        embedBuilder.addField("Sudo required", requiresAuth,true);
        return embedBuilder.build();
    }

    private MessageEmbed createEmbedForEnabledCommands(){
        EmbedBuilder embed = new EmbedBuilder();
        Command[] commands = Commands.get().commands;
        List<Command> authCommands = new ArrayList<>();
        List<Command> userCommands = new ArrayList<>();
        for(Command command : commands){
            if(!command.enabled){
                continue;
            }
            if(command.requiresAuth){
                authCommands.add(command);
            }else{
                userCommands.add(command);
            }
        }
        StringBuilder str = new StringBuilder();
        for(Command command : authCommands){
            str.append(' ').append(command.aliases[0]);
        }
        embed.addField(new MessageEmbed.Field("Auth commands", str.toString(),true));
        str.setLength(0);
        for(Command command : userCommands){
            str.append(' ').append(command.aliases[0]);
        }
        embed.addField(new MessageEmbed.Field("User commands", str.toString(),true));

        return embed.build();
    }

    public static StringBuilder formatAliases(String[] aliases){
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < aliases.length; i++){
            result.append(aliases[i]);
            if(i != aliases.length-1){
                result.append(", ");
            }
        }
        return result;
    }
}
