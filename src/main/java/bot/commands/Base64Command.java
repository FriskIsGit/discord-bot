package bot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Command extends Command {
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();

    public Base64Command(String... aliases) {
        super(aliases);
        description = "Encode/Decode base64";
        usage = "enc64 `base64`\n" +
                "dec64 `text`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
        if (args.length == 0) {
            return;
        }

        boolean invalid = false;
        byte[] bytes = args[0].getBytes(StandardCharsets.UTF_8);
        String processed = null;
        switch (commandName) {
            default:
                return;
            case "enc64":
            case "en64":
                processed = new String(encoder.encode(bytes), StandardCharsets.UTF_8);
                break;
            case "dec64":
            case "de64":
            case "un64":
                try {
                    processed = new String(decoder.decode(bytes), StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    invalid = true;
                }
                break;
        }
        if (message != null) {
            if (invalid) {
                actions.messageChannel(message.getChannel(), "Invalid sequence");
            } else {
                actions.messageChannel(message.getChannel(), processed);
            }

        }

    }
}
