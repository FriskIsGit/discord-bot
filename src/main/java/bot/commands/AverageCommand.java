package bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AverageCommand extends Command {
    public AverageCommand(String... aliases) {
        super(aliases);
        description = "Calculates the average of given numbers. It doesn't detect overflows.\n" +
                "Non-numerical arguments are not included in the average.";
        usage = "average `n1` `n2` `n3` `...`\n" +
                "avg `numbers`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
        if (args.length < 1) {
            return;
        }

        boolean messageExists = message != null;
        long sum = 0;
        int elements = 0;
        for (String arg : args) {
            Long el = parseNum(arg);
            if (el != null) {
                sum += el;
                elements++;
            }
        }
        if (elements == 0) {
            if (messageExists) {
                MessageEmbed embed = avgEmbed(0, "Division by zero");
                actions.sendEmbed(message.getChannel(), embed);
                return;
            }
        }
        if (messageExists) {
            if (elements == args.length) {
                boolean singular = elements == 1;
                MessageEmbed embed = avgEmbed(
                        sum / elements,
                        "Average of " + elements + (singular ? " element" : " elements")
                );
                actions.sendEmbed(message.getChannel(), embed);
                return;
            }
            MessageEmbed embed = avgEmbed(sum / elements, "One or more arguments couldn't be parsed");
            actions.sendEmbed(message.getChannel(), embed);
        } else {
            log.info("Average:" + (sum / elements));
        }
    }

    private static Long parseNum(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static MessageEmbed avgEmbed(long average, String title) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.valueOf(average));
        embed.setDescription(title);
        return embed.build();
    }
}
