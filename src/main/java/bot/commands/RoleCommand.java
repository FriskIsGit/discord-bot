package bot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class RoleCommand extends Command {
    public RoleCommand(String... aliases) {
        super(aliases);
        requiresAuth = true;
        description = "Grants, removes and lists roles\n" +
                "For multi-term role names enclose them in quotation marks";
        usage = "role add `user_id` `role_name`\n" +
                "role remove `user_id` `role_name`\n" +
                "role add `user_id` `role_name` `guild_id`\n" +
                "role remove `user_id` `role_name` `guild_id`\n" +
                "role list\n" +
                "role list `guild_id`\n" +
                "role ls";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
        if (args.length < 1) {
            return;
        }
        boolean msgNull = message == null;
        if (args[0].equals("list") || args[0].equals("ls")) {
            Guild guild;
            if (msgNull) {
                if (args.length == 1) {
                    return;
                }
                guild = jda.getGuildById(args[1]);
            } else {
                guild = message.getGuild();
            }

            if (guild == null) {
                return;
            }

            String roles = rolesToString(guild);
            if (msgNull) {
                log.info(roles);
            } else {
                actions.messageChannel(message.getChannel(), roles);
            }
            return;
        }

        if (args.length < 3) {
            if (!msgNull)
                actions.messageChannel(message.getChannel(), "Not enough arguments");
            return;
        }

        long id = Long.parseLong(args[1]);
        User user = jda.retrieveUserById(id).complete();
        if (user == null) {
            if (!msgNull)
                actions.messageChannel(message.getChannel(), "User couldn't be retrieved");
            return;
        }

        Guild guild = message != null ? message.getGuild() : null;
        if (args.length == 4) {
            guild = jda.getGuildById(args[3]);
        }

        if (guild == null) {
            log.warn("Not enough arguments or guild id is incorrect.");
            return;
        }

        List<Role> roles = guild.getRoles();
        Role targetRole = null;
        for (Role role : roles) {
            String roleName = role.getName();
            if (roleName.equals("@everyone"))
                continue;

            if (roleName.equals(args[2])) {
                targetRole = role;
                break;
            }
        }
        if (targetRole == null) {
            if (message != null)
                actions.messageChannel(message.getChannel(), "Role wasn't found");

            return;
        }
        if (args[0].equals("add")) {
            guild.addRoleToMember(user, targetRole).complete();
        } else if (args[0].equals("remove")) {
            guild.removeRoleFromMember(user, targetRole).complete();
        }
    }

    public static String rolesToString(Guild guild) {
        List<Role> roles = guild.getRoles();
        StringBuilder str = new StringBuilder();
        for (Role role : roles) {
            String name = role.getName();
            if (name.equals("@everyone")) {
                continue;
            }
            str.append('`');
            str.append(name);
            str.append('`');
            str.append('\n');
        }
        return str.toString();
    }
}
