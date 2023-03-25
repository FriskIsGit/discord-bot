package bot.deskort.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class InviteCommand extends Command{
    public InviteCommand(String... aliases){
        super(aliases);
        description = "Returns an existing invite link to current server";
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
        actions.messageChannel(message.getChannel(), invites.get(0).getUrl());
    }
}
