import bot.core.Bot;
import bot.music.AudioConverter;
import bot.utilities.jda.ConsoleChat;
import net.dv8tion.jda.api.entities.Guild;
import no4j.core.Logger;

import java.io.IOException;
import java.util.List;

class BotRunner {
    public static void main(String[] args) {
        try {
            Bot.initialize(args.length == 0 ? null : selectPathArgument(args));
        } catch (IOException | InterruptedException ignored) {}

        Logger log = Logger.getLogger("primary");
        log.debug("Threads active: " + Thread.activeCount());
        log.info("Bot prefix: " + Bot.PREFIX);
        log.info(connectionInformationString());


        Thread chatThread = new Thread(() -> new ConsoleChat().beginChat());
        chatThread.start();
    }

    private static String selectPathArgument(String[] args) {
        String path = null;
        for (String arg : args) {
            if (!arg.isEmpty() && arg.charAt(0) != '-') {
                path = arg;
                break;
            }
        }
        return path;
    }

    // This needs to be changed
    public static String connectionInformationString() {
        List<Guild> guilds = Bot.getJDAInterface().getGuilds();
        int size = guilds.size();
        StringBuilder format = new StringBuilder();
        format.append("Connected to ").append(size).append(" server");

        if (size == 0 || size > 1) {
            format.append("s");
        }

        if (size == 0) {
            return format.toString();
        }

        format.append(":\n[");
        for (int i = 0; i < size; i++) {
            Guild guild = guilds.get(i);
            format.append(guild.getName());
            if (i == size - 1) {
                break;
            }
            format.append(", ");
        }
        format.append("]\n[");
        for (int i = 0; i < size; i++) {
            Guild guild = guilds.get(i);
            format.append(guild.getIdLong());
            if (i == size - 1) {
                break;
            }
            format.append(", ");
        }
        format.append(']');
        return format.toString();
    }
}
