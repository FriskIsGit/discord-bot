package bot.deskort.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand extends Command{
    private static final char new_line = '\n';
    public HelpCommand(String... aliases){
        super(aliases);
        description = "Prints information about commands or a command\n" +
        "Example use case: help `join`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 0){
            String info = createInformationOnAllCommands();
            actions.messageChannel(message.getChannel(), info);
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
        String aliases = formatAliases(command.aliases).toString();
        String desc = command.description;
        String usage = command.usage;
        String enabled = String.valueOf(command.enabled);
        String requiresAuth = String.valueOf(command.requiresAuth);

        embedBuilder.addField(new MessageEmbed.Field("Aliases", aliases,false));
        if(!desc.isEmpty()){
            embedBuilder.addField(new MessageEmbed.Field("Description", desc,false));
        }
        if(!usage.isEmpty()){
            embedBuilder.addField(new MessageEmbed.Field("Usage", usage,false));
        }
        embedBuilder.addField(new MessageEmbed.Field("Enabled", enabled,true));
        embedBuilder.addField(new MessageEmbed.Field("Sudo required", requiresAuth,true));
        return embedBuilder.build();
    }

    private String createInformationOnAllCommands(){
        StringBuilder info = new StringBuilder();
        Command[] commands = Commands.get().commands;
        for(Command command : commands){
            if(!command.enabled){
                continue;
            }
            //
            info.append("Aliases: ").append(formatAliases(command.aliases)).append('|').append('\n');
            info.append("Desc: ").append(command.description).append('|').append('\n');
            info.append("Enabled: ").append(command.enabled).append('|').append('\n');
            info.append('\n');
        }

        return info.toString();
    }

    public static StringBuilder formatAliases(String[] aliases){
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < aliases.length; i++){
            result.append('\'').append(aliases[i]).append('\'');
            if(i != aliases.length-1){
                result.append(", ");
            }
        }
        return result;
    }
}
