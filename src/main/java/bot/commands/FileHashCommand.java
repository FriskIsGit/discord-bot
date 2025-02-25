package bot.commands;

import bot.utilities.Hasher;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.AttachmentProxy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FileHashCommand extends Command {
    public FileHashCommand(String... aliases) {
        super(aliases);
        description = "Hashes file attached to the message\n" +
                "using specified algorithm\n";
        usage = "file `hashing_algorithm`\n" +
                "file `hashing_algorithm` `discord_url`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
        MessageChannelUnion channel = message.getChannel();
        List<Message.Attachment> attachments = message.getMessage().getAttachments();
        if (args.length == 0) {
            actions.messageChannel(channel, "No hashing algorithm specified");
            return;
        }
        AttachmentProxy attachmentProxy;
        if (attachments.size() == 0 && args.length > 1) {
            String url = args[1];
            if (url.startsWith("https://cdn.discordapp.com")) {
                attachmentProxy = new AttachmentProxy(url);
            } else {
                actions.messageChannel(channel, "Not a discord domain");
                return;
            }
        } else if (attachments.size() > 0) {
            attachmentProxy = attachments.get(0).getProxy();
        } else {
            actions.messageChannel(channel, "No file attached or url provided.");
            return;
        }

        File tempDir = new File("tmp");
        tempDir.mkdir();
        File temp = new File("tmp/temp_file");
        temp.delete();
        try {
            temp = attachmentProxy.downloadToFile(temp).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            actions.messageChannel(channel, "Job timed out");
            return;
        }
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(temp.toPath());
        } catch (IOException e) {
            log.stackTrace("IO error on read", e);
            return;
        }
        try {
            String hash = Hasher.hashBytes(bytes, Hasher.choose(args[0]));
            actions.messageChannel(channel, hash);
        } catch (IllegalStateException exc) {
            actions.messageChannel(channel, exc.getMessage());
        } finally {
            temp.delete();
            tempDir.deleteOnExit();
        }
    }
}
