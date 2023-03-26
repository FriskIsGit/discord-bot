package bot.deskort.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

//allows for command modifications at runtime
public class ModifyCommand extends Command{
    public ModifyCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
        usage = "modify `command_alias` `op` `var` `new_value`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length < 4){
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
        switch (args[2]){
            case "requiresAuth":
                if(isGet){
                    actions.messageChannel(message.getChannel(), String.valueOf(command.requiresAuth));
                    return;
                }else{
                    command.requiresAuth = Boolean.parseBoolean(args[3]);
                }
                break;
            case "enabled":
                if(isGet){
                    actions.messageChannel(message.getChannel(), String.valueOf(command.enabled));
                    return;
                }else{
                    command.enabled = Boolean.parseBoolean(args[3]);
                }
                break;
            case "timesExecuted":
                if(isGet){
                    actions.messageChannel(message.getChannel(), String.valueOf(command.timesExecuted));
                    return;
                }else{
                    command.timesExecuted = Integer.parseInt(args[3]);
                }
                break;
            default:
                actions.messageChannel(message.getChannel(), "No variable matched");
                return;
        }
        actions.messageChannel(message.getChannel(), "Value changed to " + args[3]);
    }
}
