package bot.commands.filebin;

import bot.commands.Command;
import bot.utilities.RandomString;
import kong.unirest.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FileBinCommand extends Command {
    private static final String FILE_BIN_NET = "https://filebin.net";
    private static final int socketTimeoutSeconds = 4, connectTimeoutSeconds = 6, downloadTimeoutSeconds = 15;
    private static final RandomString randomString = new RandomString(true, true, true);

    private MessageChannelUnion channel;

    public FileBinCommand(String... aliases) {
        super(aliases);
        description = "Manages filebin API, performs uploads, deletions or retrievals.\n" +
                "**Timeouts**: \n" +
                "- downloadTimeout - " + downloadTimeoutSeconds + "s\n" +
                "- socketTimeout - " + socketTimeoutSeconds + "s\n" +
                "- connectTimeout - " + connectTimeoutSeconds + "s\n";
        usage = "filebin post <file_attachment>\n" +
                "filebin upload <file_attachment>\n" +
                "filebin info `bin_id`\n" +
                "filebin get `bin_id`\n" +
                "filebin delete `bin_id`\n" +
                "filebin delete `bin_url`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
        if (message != null) {
            channel = message.getChannel();
        }
        if (args.length == 0) {
            return;
        }

        switch (args[0]) {
            case "post":
            case "upload":
                if (message == null) {
                    break;
                }
                List<Message.Attachment> attachments = message.getMessage().getAttachments();
                if (attachments.size() == 0) {
                    actions.sendEmbed(channel, createInfoEmbed("No attachments available", ""));
                    break;
                }
                File tempDir = new File("tmp");
                tempDir.mkdir();
                File temp = new File("tmp/temp_file");
                Message.Attachment attachment = attachments.get(0);
                boolean success = downloadAttachment(attachment, temp);
                if (!success) {
                    break;
                }
                BinPostResult bin = doFileBinPost(temp, attachment.getFileName());
                temp.delete();
                if (bin.exceptionThrown) {
                    actions.sendEmbed(channel, createInfoEmbed("Error", "An exception was thrown"));
                    break;
                }

                HttpResponse<String> response = bin.response;
                int code = response.getStatus();
                String body = response.getBody();
                if (code == 201 || code == 200) {
                    actions.sendEmbed(channel, createInfoEmbed("Bin created", bin.url));
                } else {
                    actions.sendEmbed(channel, createInfoEmbed(code, body));
                }
                break;
            case "info":
            case "get":
                if (args.length == 1) {
                    actions.sendEmbed(channel, createInfoEmbed("No bin_id provided", "Not enough arguments"));
                    break;
                }
                //no bin found
                HttpResponse<String> resp = doFileBinGet(args[1]);
                if (resp == null) {
                    actions.sendEmbed(channel, createInfoEmbed("Error", "An exception was thrown"));
                    break;
                }

                code = resp.getStatus();
                body = resp.getBody();
                if (code != 200 && code != 201) {
                    actions.sendEmbed(channel, createInfoEmbed(code, body));
                    break;
                }
                System.out.println(body);
                BinInfo info = new BinInfo(body);
                actions.sendEmbed(channel, createBinEmbed(info));
                break;
            case "delete":
            case "del":
                if (args.length == 1) {
                    actions.sendEmbed(channel, createInfoEmbed("No bin_id provided", "Not enough arguments"));
                    break;
                }
                doFileBinDelete(args[1]);
                break;
            default:
                actions.sendEmbed(channel, createInfoEmbed("Invalid operation", "Choices: post, get, delete"));
                break;
        }

    }

    // Unhandled NPE
    private void doFileBinDelete(String bin) {
        String url;
        if (bin.startsWith(FILE_BIN_NET)) {
            url = bin;
        } else {
            url = FILE_BIN_NET + '/' + bin;
        }

        HttpRequestWithBody request = Unirest.delete(url);
        HttpResponse<String> response = request.asString();
        int code = response.getStatus();
        String body = response.getBody();
        if (code == 200) {
            actions.sendEmbed(channel, createInfoEmbed("OK", body));
        } else {
            actions.sendEmbed(channel, createInfoEmbed(code, body));
        }
    }

    private HttpResponse<String> doFileBinGet(String bin) {
        if (bin.startsWith(FILE_BIN_NET)) {
            bin = extractId(bin);
        }

        GetRequest request = Unirest.get(FILE_BIN_NET + '/' + bin).header("Accept", "application/json");
        try {
            return request.asString();
        } catch (UnirestException e) {
            return null;
        }
    }

    private String extractId(String bin) {
        int linkLen = bin.length();
        int start = FILE_BIN_NET.length() + 1;
        int end = bin.indexOf('/', start);
        if (start >= linkLen) {
            throw new IllegalArgumentException("Bin does not contain an id");
        }
        if (end == -1) {
            end = linkLen;
        }
        return bin.substring(start, end);
    }

    private boolean downloadAttachment(Message.Attachment attachment, File temp) {
        try {
            attachment.getProxy().downloadToFile(temp).get(downloadTimeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            actions.messageChannel(channel, "Job timed out");
            return false;
        }
        return true;
    }

    public static BinPostResult doFileBinPost(File file, String filename) {
        String bin = randomString.nextString(8);
        String url = FILE_BIN_NET + '/' + bin + '/' + filename;
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            return BinPostResult.fail();
        }
        RequestBodyEntity request = Unirest.post(FILE_BIN_NET + '/' + bin + '/' + filename)
                .body(fileBytes)
                .socketTimeout(socketTimeoutSeconds * 1000)
                .connectTimeout(connectTimeoutSeconds * 1000);
        try {
            HttpResponse<String> response = request.asString();
            return BinPostResult.ok(url, response);
        } catch (UnirestException e) {
            return BinPostResult.fail();
        }
    }

    private static MessageEmbed createInfoEmbed(int code, String desc) {
        return createInfoEmbed(String.valueOf(code), desc);
    }

    private static MessageEmbed createInfoEmbed(String title, String desc) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        if (!desc.isEmpty()) {
            embed.setDescription(desc);
        }
        embed.setDescription(desc);
        return embed.build();
    }

    private static MessageEmbed createBinEmbed(BinInfo info) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(info.getURL());
        embed.addField("Expires", info.expires, true)
                .addField("Type", info.contentType, true)
                .addField("Id", info.id, false)
                .addField("Size", info.contentSize, true)
                .addField("Bytes", String.valueOf(info.allBytes), true);
        return embed.build();
    }
}
