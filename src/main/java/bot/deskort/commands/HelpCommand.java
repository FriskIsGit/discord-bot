package bot.deskort.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand extends Command{

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
            actions.messageChannel(message.getChannel(), args[0] + " doesn't map to any command");
            return;
        }
        actions.messageChannel(message.getChannel(), createInformationAboutCommand(command));
    }

    private String createInformationAboutCommand(Command command){
        return "Aliases: " + formatAliases(command.aliases) + '\n' +
                "Desc: " + command.description +  '\n' +
                "Enabled: " + command.enabled + '\n';
    }

    private String createInformationOnAllCommands(){
        StringBuilder info = new StringBuilder();
        Command[] commands = Commands.get().commands;
        for(Command command : commands){
            if(!command.enabled){
                continue;
            }
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
