import bot.core.Bot;
import bot.utilities.jda.ConsoleChat;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

class BotRunner{
    public static void main(String[] args){
        try{
            Bot.initialize(args.length == 0 ? null : selectPathArgument(args));
        }catch (InterruptedException ignored){}

        System.out.println(connectionInformationString());
        System.out.println("Threads active: " + Thread.activeCount());
        System.out.println("Bot prefix: " + Bot.PREFIX);

        Thread chatThread = new Thread(() -> new ConsoleChat().beginChat());
        chatThread.start();
    }

    private static String selectPathArgument(String[] args){
        String path = null;
        for (String arg : args){
            if (!arg.isEmpty() && arg.charAt(0) != '-'){
                path = arg;
                break;
            }
        }
        return path;
    }

    public static String connectionInformationString(){
        List<Guild> guilds = Bot.getJDAInterface().getGuilds();
        int size = guilds.size();
        StringBuilder servers = new StringBuilder();
        servers.append("Connected to ").append(size).append(" servers:\n");
        if(size == 0){
            return servers.toString();
        }
        servers.append('[');
        for (int i = 0; i < size; i++){
            Guild guild = guilds.get(i);
            servers.append(guild.getName());
            if(i == size-1){
                break;
            }
            servers.append(", ");
        }
        servers.append("]\n[");
        for (int i = 0; i < size; i++){
            Guild guild = guilds.get(i);
            servers.append(guild.getIdLong());
            if(i == size-1){
                break;
            }
            servers.append(", ");
        }
        servers.append(']');
        return servers.toString();
    }
}
