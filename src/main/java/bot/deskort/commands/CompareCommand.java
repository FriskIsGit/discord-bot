package bot.deskort.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CompareCommand extends Command{
    public CompareCommand(String... aliases){
        super(aliases);
        description = "Compares character sequence, quotations can be used";
        usage = "compare `sequence1` `sequence2`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length < 2){
            actions.messageChannel(message.getChannel(), "Nothing to compare to");
            return;
        }
        int len = args[0].length();
        if(len != args[1].length()){
            actions.messageChannel(message.getChannel(), "Lengths differ.");
            return;
        }

        String first = args[0], second = args[1];
        for (int i = 0; i < len; i++){
            if(first.charAt(i) != second.charAt(i)){
                actions.messageChannel(message.getChannel(),
                        "Character mismatch `" + first.charAt(i) + "` and `" + second.charAt(i) +
                                "` at index: " + i);
                return;
            }
        }
        actions.messageChannel(message.getChannel(), "Equal.");

    }
}
