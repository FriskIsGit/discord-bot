package bot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HttpCommand extends Command {
    private static final String httpCatAddress = "https://http.cat/";

    public HttpCommand(String... aliases) {
        super(aliases);
        description = "Displays HTTP status codes as cats";
        usage = "http `code`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {

        if (args.length == 0) {
            return;
        }
        try {
            Integer.parseInt(args[0]);
        } catch (NumberFormatException nfExc) {
            System.out.println("Parsing failure for:" + args[0]);
            return;
        }
        actions.messageChannel(message.getChannel(), httpCatAddress + args[0]);
    }
}
