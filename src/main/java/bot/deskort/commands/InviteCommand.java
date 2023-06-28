package bot.deskort.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class InviteCommand extends Command{
    public InviteCommand(String... aliases){
        super(aliases);
        description = "Returns an existing invite link to current server";
        usage = "invite\n" +
                "invite -all\n" +
                "invite `quantity`";
        triggerableByBot = true;
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        Guild guild = message.getGuild();
        boolean disabled = guild.isInvitesDisabled();
        if(disabled){
            actions.messageChannel(message.getChannel(), "Invites are disabled");
            return;
        }
        List<Invite> invites = guild.retrieveInvites().complete();
        if(invites.size() == 0){
            actions.messageChannel(message.getChannel(), "No invite links exist");
            return;
        }
        boolean all = invites.size() > 1 && args.length == 1 && args[0].equals("-all");
        int quantity;
        if(all){
            quantity = invites.size();
        }else{
            quantity = args.length == 1 ? parseInt(args[0]) : 1;
            quantity = Math.min(invites.size(), quantity);
        }
        
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < quantity; i++){
            String url = invites.get(i).getUrl();
            str.append(url);
            if(i != quantity -1){
                str.append('\n');
            }
        }

        actions.messageChannel(message.getChannel(), str.toString());
    }

    private static int parseInt(String num){
        try{
            return Integer.parseInt(num);
        }catch (NumberFormatException e){
            return 1;
        }
    }
}
