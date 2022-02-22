package bot.utilities;

import bot.deskort.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Servers {

    private Guild currentGuild;
    private static JDA jdaInterface;
    public Servers(){
        jdaInterface = Bot.getJDAInterface();
        List<Guild> guilds = getServers();
        if(guilds.size() < 1){
            System.err.println("Bot is not on any server");
            System.exit(0);
        }
        currentGuild = guilds.get(0);
    }

    public Guild getServer(long id){
        return jdaInterface.getGuildById(id);
    }
    public Guild getServer(String partialName){
        List<Guild> servers = getServers();
        for (Guild server : servers){
            if (server.getName().contains(partialName)){
                currentGuild = server;
                return currentGuild;
            }
        }
        return null;
    }
    public Guild getServerIgnoreCase(String partialName){
        partialName = partialName.toLowerCase(Locale.ROOT);
        List<Guild> servers = getServers();
        for (Guild server : servers){
            if (server.getName().toLowerCase(Locale.ROOT).contains(partialName)){
                currentGuild = server;
                return currentGuild;
            }
        }
        return currentGuild;
    }
    public List<Guild> getServers(){
        return jdaInterface.getGuilds();
    }
    public List<String> getServerNames(){
        List<Guild> guilds = getServers();
        int numberOfGuilds = guilds.size();
        List<String> serverNames = new ArrayList<>(numberOfGuilds);
        for (Guild guild : guilds){
            serverNames.add(guild.getName());
        }
        return serverNames;
    }
}
