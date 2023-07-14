package bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ManageCommand extends Command{
    private static final String ENABLED = "enabled", AUTH = "requiresAuth", EXECUTIONS = "timesExecuted", DESC = "description";
    public ManageCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
        description = "Allows command modifications at runtime\n" +
                      "Symbol definitions:\n " +
                      "`op`  - set/get\n" +
                      "`var` - enabled/requiresAuth/timesExecuted/description";
        usage = "manage `command_alias` `op` `var` `new_value`\n" +
                "manage `command_alias` `op` `var`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length < 3){
            return;
        }
        Command command = Commands.get().command(args[0]);
        if(command == null){
            actions.messageChannel(message.getChannel(), "Command wasn't matched");
            return;
        }
        boolean isGet;
        switch (args[1]){
            case "get":
                isGet = true;
                break;
            case "set":
                isGet = false;
                break;
            default:
                actions.messageChannel(message.getChannel(), "No opcode matched");
                return;
        }

        if(!isGet && args.length != 4){
            return;
        }

        String varName;
        switch (args[2]){
            case AUTH:
                if(isGet){
                    actions.messageChannel(message.getChannel(), String.valueOf(command.requiresAuth));
                    return;
                }else{
                    varName = AUTH;
                    command.requiresAuth = Boolean.parseBoolean(args[3]);
                }
                break;
            case ENABLED:
                if(isGet){
                    actions.messageChannel(message.getChannel(), String.valueOf(command.enabled));
                    return;
                }else{
                    varName = ENABLED;
                    command.enabled = Boolean.parseBoolean(args[3]);
                }
                break;
            case EXECUTIONS:
                if(isGet){
                    actions.messageChannel(message.getChannel(), String.valueOf(command.timesExecuted));
                    return;
                }else{
                    varName = EXECUTIONS;
                    command.timesExecuted = Integer.parseInt(args[3]);
                }
                break;
            case DESC:
                if(isGet){
                    actions.messageChannel(message.getChannel(), command.description);
                    return;
                }else{
                    varName = DESC;
                    command.description = args[3];
                }
                break;
            default:
                actions.messageChannel(message.getChannel(), "No variable matched");
                return;
        }
        actions.sendEmbed(message.getChannel(), createModifiedEmbed(varName, args[3]));
    }
    private static MessageEmbed createModifiedEmbed(String varName, String newValue){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(varName);
        embed.setDescription("New value: " + newValue);
        return embed.build();
    }
}
