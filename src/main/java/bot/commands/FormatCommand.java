package bot.commands;

import bot.utilities.formatters.JavaFormatter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class FormatCommand extends Command {
    public FormatCommand(String... aliases) {
        super(aliases);
        description = "Embeds code. Provided code should be enclosed in grave accent ` (U+0060).\n" +
                "One grave character at the beginning of the code and one at the end";
        usage = "format `lang` `code`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
        System.out.println("NUMBER OF ARGS:  " + args.length);
        if (args.length < 2 || message == null) {
            return;
        }
        StringBuilder code = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            code.append(' ').append(args[i]);
        }
        String formatted = JavaFormatter.format(code.toString());
        actions.sendEmbed(message.getChannel(), embedCode(args[0], formatted));
    }

    private static MessageEmbed embedCode(String lang, String code) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Code");
        embed.setDescription(putInCodeBlock(lang, code));
        return embed.build();
    }

    private static String putInCodeBlock(String lang, String code) {
        return "```" + lang + '\n' +
                code + "```";
    }
}
