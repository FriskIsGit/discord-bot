package bot.commands;

import bot.core.Bot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Iterator;
import java.util.List;

public class SudoCommand extends Command {
    public SudoCommand(String... aliases) {
        super(aliases);
        description = "Grants or revokes sudo privileges from users at runtime";
        //unsafe
        usage = "sudo grant `@mention`\n" +
                "sudo revoke `@mention`\n" +
                "sudo `action` `user_id`\n" +
                "sudo list\n" +
                "sudo ls";
        requiresAuth = true;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
        if (args.length == 0) {
            return;
        }
        boolean msgNotNull = message != null;

        if (msgNotNull && (args[0].equals("list") || args[0].equals("ls"))) {
            actions.sendAsMessageBlock(message.getChannel(), sudoIdsToString());
            return;
        }
        if (args.length < 2) {
            return;
        }

        long id;
        Member member = null;
        boolean isMentioned = false;
        List<Member> members = null;
        if (msgNotNull) {
            members = message.getMessage().getMentions().getMembers();
            isMentioned = members.size() == 1;
        }

        if (isMentioned) {
            member = members.get(0);
            id = member.getIdLong();
        } else {
            id = Long.parseLong(args[1]);
        }

        switch (args[0]) {
            case "grant":
                Bot.AUTHORIZED_USERS.add(id);
                if (isMentioned) {
                    actions.sendAsMessageBlock(
                            message.getChannel(),
                            "Sudo granted to " + member.getUser().getAsTag());
                } else if (msgNotNull) {
                    actions.sendAsMessageBlock(
                            message.getChannel(),
                            "Sudo granted to " + id);
                }
                break;
            case "revoke":
                Bot.AUTHORIZED_USERS.remove(id);
                if (isMentioned) {
                    actions.sendAsMessageBlock(
                            message.getChannel(),
                            "Sudo revoked from " + member.getUser().getAsTag());
                } else if (msgNotNull) {
                    actions.sendAsMessageBlock(
                            message.getChannel(),
                            "Sudo revoked from " + id);
                }
                break;
            default:
        }
    }

    private String sudoIdsToString() {
        Iterator<Long> ids = Bot.AUTHORIZED_USERS.iterator();
        StringBuilder str = new StringBuilder();
        while (ids.hasNext()) {
            str.append(ids.next());
            if (!ids.hasNext()) {
                break;
            }
            str.append(", ");
        }
        return str.toString();
    }
}
