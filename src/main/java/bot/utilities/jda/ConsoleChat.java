package bot.utilities.jda;

import bot.core.Bot;
import bot.commands.Command;
import bot.commands.Commands;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.File;
import java.util.Scanner;

public class ConsoleChat {
    private final static int USER_ID_LEN = 18;
    private final Scanner scanner = new Scanner(System.in);

    private boolean inServer = false, looping = true;
    private TextChannel currentChannel = null;
    private long dmId = 0;

    private final Actions actions;

    public ConsoleChat() {
        actions = Bot.getActions();
    }

    public void quit() {
        looping = false;
    }

    public void beginChat() {
        while (looping) {
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                continue;
            }
            if (input.equals("exit")) {
                quit();
                break;
            }
            if (!input.startsWith(Bot.PREFIX)) {
                if (inServer) {
                    actions.messageChannel(currentChannel, input);
                } else {
                    if (dmId == 0) {
                        System.err.println("Unset dm id");
                        continue;
                    }
                    actions.messageUser(dmId, input);
                }
                continue;
            }
            String[] args = Commands.doubleTermSplit(input, Bot.PREFIX_OFFSET);
            switch (args[0]) {
                case "cc":
                    changeChannel(args);
                    break;
                case "lc":
                    listChannels(args);
                    break;
                case "dm":
                    switchToDm(args);
                    break;
                case "file":
                    sendFile(args);
                    break;
                case "say":
                    actions.messageChannel(currentChannel, args[1]);
                    break;
                case "where": {
                    if (inServer) System.out.println("Current channel: " + currentChannel);
                    else System.out.println("In dm, id: " + dmId);
                    break;
                }
                default:
                    String[] remainingArgs = Commands.splitIntoTerms(args[1]);
                    try {
                        Command command = Commands.get().command(args[0]);
                        if (command == null) {
                            System.err.println("Command not found, name: " + args[0]);
                            continue;
                        }
                        command.executeUnrestricted(args[0], remainingArgs);
                    } catch (Exception exc) {
                        System.err.println(exc.getMessage());
                    }
            }
        }
    }

    private void sendFile(String[] args) {
        //file path
        File file = new File(args[1]);
        if (inServer) {
            actions.sendFile(currentChannel, file);
        } else {
            actions.sendFileToUser(dmId, file);
        }
    }

    private void switchToDm(String[] anySplit) {
        String toParse = anySplit[1];
        if (!toParse.matches("[0-9]+")) {
            System.err.println("Provided ID is not numerical");
            return;
        }
        if (toParse.length() != USER_ID_LEN) {
            System.err.println("ID should be exactly 18 digits long");
            return;
        }
        dmId = Long.parseLong(toParse);
        System.out.println("Switched to dm, id: " + dmId);
        inServer = false;
    }

    private void listChannels(String[] args) {
        //server name required
        if (args.length == 1) {
            return;
        }
        Guild server = actions.getServerIgnoreCase(args[1]);
        if (server == null) {
            return;
        }
        System.out.println(server.getTextChannels());
    }

    private void changeChannel(String[] args) {
        if (args.length == 0 || args[1].isEmpty()) {
            return;
        }
        TextChannel nextChannel = actions.getTextChannel(args[1]);
        if (nextChannel != null) {
            currentChannel = nextChannel;
            System.out.println("Moved to: " + nextChannel.getName());
            inServer = true;
        }
    }
}
