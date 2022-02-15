package bot.utilities;

import bot.deskort.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Set;
//general model
public class Permissions{
    private final JDA jdaInterface;
    public Permissions(){
        this.jdaInterface = Bot.getJDAInterface();
    }

    public boolean hasPermission(final String permissionName, final long userId){
        boolean memberFound = false;
        List<Guild> serversList = jdaInterface.getGuilds();
        User user = jdaInterface.getUserById(userId);
        if (user == null){
            System.err.println("Invalid user id");
            return false;
        }
        for (Guild server : serversList){
            Member serverMember = server.getMember(user);
            if (serverMember != null){
                memberFound = true;
                Set<Permission> permissionSet = serverMember.getPermissions();
                for (Permission perm : permissionSet){
                    if (perm.getName().equals(permissionName)){
                        return true;
                    }
                }
            }
        }
        if (!memberFound){
            System.err.println("Member wasn't found in any connected servers");
        }
        return false;
    }

    public boolean canDeleteMessages(long userId){
        return hasPermission("Manage Messages", userId);
    }

    public boolean canBan(long userId){
        return hasPermission("Ban Members", userId);
    }

    public boolean canKick(long userId){
        return hasPermission("Kick Members", userId);
    }

    public boolean isAdministrator(long userId){
        return hasPermission("Administrator", userId);
    }

}
