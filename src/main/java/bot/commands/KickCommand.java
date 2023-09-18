package bot.commands;

import bot.utilities.Option;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class KickCommand extends Command{
    public KickCommand(String... aliases){
        super(aliases);
        usage = "kick `user_id`\n" +
                "kick `user_id` `reason`";
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 0 || message == null){
            return;
        }
        if(args[0].length() < 17 || args[0].length() > 19){
            actions.messageChannel(message.getChannel(), "Invalid id length");
            return;
        }
        Option<Long> maybeId = parseLong(args[0]);
        if(!maybeId.isSome()){
            return;
        }
        long id = maybeId.unwrap();
        String reason = args.length > 1 ? args[1] : null;
        User user = jda.getUserById(id);
        //if not cached - retrieve
        if(user == null){
            try{
                user = jda.retrieveUserById(id).complete();
            }catch (RuntimeException unknownUser){
                System.err.println("UNKNOWN USER");
                return;
            }
        }

        actions.kickUser(user, message.getGuild(), reason);
    }
    private static Option<Long> parseLong(String num){
        try{
            return Option.of(Long.parseLong(num));
        }catch (NumberFormatException e){
            return Option.none();
        }
    }
}
