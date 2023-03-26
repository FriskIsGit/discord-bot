package bot.deskort.commands;

import bot.deskort.Bot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class SudoCommand extends Command{
    public SudoCommand(String... aliases){
        super(aliases);
        description = "Grants or revokes sudo privileges from users at runtime";
        //unsafe
        usage = "sudo grant `@mention`\n" +
                "sudo revoke `@mention`\n";
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length < 2){
            return;
        }
        List<Member> members = message.getMessage().getMentions().getMembers();
        if (members.size() != 1){
            return;
        }

        Member member = members.get(0);
        switch (args[0]){
            case "grant":
                Bot.AUTHORIZED_USERS.add(member.getIdLong());
                actions.sendAsMessageBlock(
                        message.getChannel(),
                        "Sudo granted to " + member.getUser().getAsTag()
                );
                break;
            case "revoke":
                Bot.AUTHORIZED_USERS.remove(member.getIdLong());
                actions.sendAsMessageBlock(
                        message.getChannel(),
                        "Sudo revoked from " + member.getUser().getAsTag()
                );
                break;
            default:
                return;
        }

    }
}
