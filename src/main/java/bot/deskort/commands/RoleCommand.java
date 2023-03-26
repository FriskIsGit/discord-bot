package bot.deskort.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class RoleCommand extends Command{
    public RoleCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
        description = "Grants, removes and lists roles\n" +
                      "For multi-term role names enclose them in quotation marks";
        usage = "role add `user_id` `role_name`\n" +
                "role remove `user_id` `role_name`\n" +
                "role list";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 1){
            if(args[0].equals("list")){
                String roles = rolesToString(message.getGuild());
                actions.messageChannel(message.getChannel(), roles);
            }
            return;
        }
        if(args.length < 3){
            actions.messageChannel(message.getChannel(), "Not enough arguments");
            return;
        }

        long id = Long.parseLong(args[1]);
        User user = jda.retrieveUserById(id).complete();
        if(user == null){
            actions.messageChannel(message.getChannel(), "User couldn't be retrieved");
            return;
        }
        Guild guild = message.getGuild();
        List<Role> roles = guild.getRoles();
        Role targetRole = null;
        for(Role role : roles){
            String roleName = role.getName();
            if(roleName.equals("@everyone")){
                continue;
            }
            if(roleName.equals(args[2])){
                targetRole = role;
                break;
            }
        }
        if(targetRole == null){
            actions.messageChannel(message.getChannel(), "Role wasn't found");
            return;
        }
        if(args[0].equals("add")){
            guild.addRoleToMember(user, targetRole).complete();
        }else if(args[0].equals("remove")){
            guild.removeRoleFromMember(user, targetRole).complete();
        }
    }

    public static String rolesToString(Guild guild){
        List<Role> roles = guild.getRoles();
        StringBuilder str = new StringBuilder();
        for(Role role : roles){
            String name = role.getName();
            if(name.equals("@everyone")){
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
