package bot.deskort.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class BanCommand extends Command{
    public BanCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
        description = "Bans user";
        usage = "ban `user_id`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 0){
            actions.messageChannel(message.getChannel(), "No id specified");
            return;
        }
        User userToBan;
        try{
            userToBan = jda.retrieveUserById(args[0]).complete();
        }catch (IllegalArgumentException illegalArgExc){
            return;
        }
        boolean res = actions.banUser(userToBan, message.getGuild());
        if(res){
            actions.messageChannel(message.getChannel(), "User banned :skull:");
        }
    }
}
