package bot.utilities;

import bot.deskort.Bot;
import bot.deskort.commands.Command;
import bot.deskort.commands.Commands;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.File;
import java.util.Scanner;

public class ConsoleChat{
    private final Scanner scanner = new Scanner(System.in);

    private boolean inServer = false, looping = true;
    private TextChannel currentChannel = null;
    private long dmId = 0;

    private final Actions actions;

    public ConsoleChat(){
        actions = Bot.getActions();
    }

    public void quit(){
        looping = false;
    }

    public void beginChat(){
        while(looping) {
            String input = scanner.nextLine();
            if(input.equals("exit")){
                quit();
                break;
            }
            if(!input.startsWith(Bot.PREFIX)){
                if(inServer){
                    actions.messageChannel(currentChannel, input);
                }else{
                    if(dmId == 0){
                        System.err.println("Unset dm id");
                        continue;
                    }
                    actions.messageUser(dmId, input);
                }
                continue;
            }
            String[] args = Commands.doubleTermSplit(input, Bot.PREFIX_OFFSET);
            switch (args[0]){
                case "cc":
                    changeChannel(args);
                    break;
                case "lc":
                    listChannels(args);
                    break;
                case "dm":
                    switchToDm(args);
                case "file":
                    sendFile(args);
                case "where": {
                    if (inServer)
                        System.out.println("Current channel: " + currentChannel);
                    else System.out.println("In dm, id: " + dmId);
                    break;
                }
                default:
                    String[] remainingArgs = Commands.splitIntoTerms(args[1]);
                    try{
                        Command command = Commands.get().command(args[0]);
                        if(command == null){
                            System.err.println("Command not found, name: " + args[0]);
                            continue;
                        }
                        command.executeUnrestricted(args[0], remainingArgs);
                    }catch (Exception exc){
                        System.err.println(exc.getMessage());
                    }
            }
        }
        System.out.println("Exited chat.");
    }

    private void sendFile(String[] args){
        //file path
        File file = new File(args[1]);
        if(inServer){
            actions.sendFile(currentChannel, file);
        }else{
            actions.sendFileToUser(dmId, file);
        }
    }

    private void switchToDm(String[] anySplit){
        //user id required
        try{
            dmId = Long.parseLong(anySplit[1]);
            System.out.println("Switched to dm, id: " + dmId);
            inServer = false;
        }catch (NumberFormatException numFormatExc){
            System.out.println("Failed to parse id");
        }
    }

    private void listChannels(String[] args){
        //server name required
        if(args.length == 1){
            return;
        }
        Guild server = actions.getServerIgnoreCase(args[1]);
        if (server == null){
            return;
        }
        System.out.println(server.getTextChannels());
    }

    private void changeChannel(String[] args){
        if(args.length == 0 || args[1].isEmpty()){
            return;
        }
        TextChannel nextChannel = actions.getTextChannel(args[1]);
        if(nextChannel != null){
            currentChannel = nextChannel;
            System.out.println("Moved to: " + nextChannel.getName());
            inServer = true;
        }
    }
}
