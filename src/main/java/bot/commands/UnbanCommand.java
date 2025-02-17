package bot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UnbanCommand extends Command {
    private static final boolean allowCrossGuild = true;

    public UnbanCommand(String... aliases) {
        super(aliases);
        requiresAuth = true;
        description = "Unbans user by id or mention";
        usage = "unban `userId`\n" +
                "unban `@mention`\n" +
                "unban `userId` `guildId`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
        if (args.length == 0) {
            actions.messageChannel(message.getChannel(), "No `id` specified");
            return;
        }
        User userToUnban;
        try {
            userToUnban = jda.retrieveUserById(args[0]).complete();
        } catch (IllegalArgumentException illegalArgExc) {
            System.err.println(illegalArgExc.getMessage());
            return;
        }
        //id
        Guild guild;
        if (args.length >= 2 && allowCrossGuild) {
            long id = Long.parseLong(args[1]);
            guild = actions.getGuildById(id);
        } else {
            guild = message.getGuild();
        }
        boolean res = actions.unbanUser(userToUnban, guild);
        if (res) {
            actions.messageChannel(message.getChannel(), "User unbanned :right_facing_fist:");
        }
    }
}
