package bot.commands;

import bot.utilities.AttachedFile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.AttachmentProxy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConvertCommand extends Command {
    private static final int conversionTimeout = 22;
    private static final int downloadTimeout = 8;
    private static final boolean onlyAllowDiscordAttachments = false, cleanupWhenDone = true;
    private static final String DISCORD_ATTACHMENT_URL = "https://cdn.discordapp.com/attachments";
    private Message embedMessage;

    public ConvertCommand(String... aliases) {
        super(aliases);
        description = "Converts media using ffmpeg.\n" +
                "Timeouts: \n" +
                " - conversion = " + conversionTimeout + "s" +
                " - download = " + downloadTimeout + "s";
        usage = "convert `new_format` <file attachment>\n" +
                "convert `new_format` `file_url`";
    }

    //since ffmpeg locks up when converting video for an unspecified amount of time it its input stream is not read,
    //it is unreliable to use waitFor and let it hang indefinitely
    //when dealing with audio the output is smaller therefore miraculously it doesn't hang
    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args) {
        if (message == null) {
            System.err.println("Message is null, this command requires a file attachment to work");
            return;
        }
        MessageChannelUnion channel = message.getChannel();
        if (args.length < 1) {
            actions.messageChannel(channel, "Insufficient number of arguments");
            return;
        }

        List<Message.Attachment> attachments = message.getMessage().getAttachments();
        boolean noAttachment = attachments.size() == 0;
        if (noAttachment && args.length == 1) {
            actions.messageChannel(channel, "No file attached or url provided");
            return;
        }

        AttachedFile resource;
        if (noAttachment) {
            //url provided as argument
            resource = AttachedFile.parse(args[1]);
        } else {
            Message.Attachment attachment = attachments.get(0);
            resource = AttachedFile.fromAttachment(attachment);
        }
        if (onlyAllowDiscordAttachments) {
            if (!resource.url.startsWith(DISCORD_ATTACHMENT_URL)) {
                actions.messageChannel(channel, "Only discord attachments are allowed.");
                return;
            }
        }

        MessageEmbed embed = createJobEmbed(resource.extension, args[0]);
        embedMessage = channel.sendMessageEmbeds(embed).complete();

        long st = System.currentTimeMillis();
        boolean success = downloadAndConvert(message.getMessage(), resource, args[0]);
        long en = System.currentTimeMillis();
        if (success) {
            MessageEmbed completedEmbed = createCompletionEmbed(en - st);
            channel.editMessageEmbedsById(embedMessage.getId(), completedEmbed).queue();
        }
    }

    private boolean downloadAndConvert(Message requestMessage, AttachedFile attachedFile, String newExtension) {
        MessageChannelUnion channel = requestMessage.getChannel();
        File tempDir = new File("tmp");
        tempDir.mkdir();
        File temp = new File(tempDir.getAbsolutePath() + "/" + attachedFile.name);
        temp.delete();
        File converted = new File(tempDir.getAbsolutePath() + "/out." + newExtension);
        converted.delete();

        AttachmentProxy proxy = new AttachmentProxy(attachedFile.url);
        try {
            temp = proxy.downloadToFile(temp).get(downloadTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            MessageEmbed timeoutEmbed = createTimeoutEmbed(e.getMessage());
            channel.editMessageEmbedsById(embedMessage.getId(), timeoutEmbed).queue();
            return false;
        }

        ProcessBuilder procBuilder = new ProcessBuilder();
        try {
            //bash -c 'args'
            procBuilder.command("ffmpeg", "-y", "-i", normalize(temp.getAbsolutePath()), normalize(converted.getAbsolutePath()));
            System.out.println("COMMAND: " + procBuilder.command());
            Process process = procBuilder.start();
            readInputToAvoidHang(process, 50);
            long waitBegin = System.currentTimeMillis();
            process.waitFor(conversionTimeout, TimeUnit.SECONDS);
            long waitTime = System.currentTimeMillis() - waitBegin;
            System.out.println("Waited for " + waitTime + " ms");

            if (converted.length() == 0) {
                channel.editMessageEmbedsById(embedMessage.getId(), createEmptyFileEmbed(waitTime, process.isAlive())).queue();
                return false;
            } else {
                actions.sendFile(requestMessage.getChannel(), converted);
            }


        } catch (IOException e) {
            e.printStackTrace();
            String msg = e.getMessage();
            if (msg.startsWith("Cannot run program")) {
                channel.editMessageEmbedsById(embedMessage.getId(), createNoProgramEmbed()).queue();
            }
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            //this aims to reduce the clutter of files left over as a result of lots of downloads
            if (cleanupWhenDone) {
                temp.delete();
                tempDir.deleteOnExit();
                converted.delete();
            }
        }
        return true;
    }

    //on some systems (like Windows), the process will hang if streams are not read
    private void readInputToAvoidHang(Process proc, int intervalMs) {
        new Thread(() -> {
            long lastRead = 0;
            //in case of ffmpeg errorOutput is treated like normalOutput
            InputStream errorOutput = proc.getErrorStream();
            InputStream normalOutput = proc.getInputStream();
            while (proc.isAlive()) {
                if (System.currentTimeMillis() - lastRead > intervalMs) {
                    //read
                    try {
                        int available = errorOutput.available();
                        if (available != -1) {
                            byte[] outBytes = new byte[available];
                            int ignored = errorOutput.read(outBytes);
                            //alternatively process output can be printed with System.out.print(new String(outBytes));
                        }
                        available = normalOutput.available();
                        if (available != -1) {
                            byte[] outBytes = new byte[available];
                            int ignored = normalOutput.read(outBytes);
                            //alternatively process output can be printed with System.out.print(new String(outBytes));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    lastRead = System.currentTimeMillis();
                }
            }
        }).start();
    }

    private MessageEmbed createJobEmbed(String originalExt, String targetExt) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Attempting job..");
        embed.appendDescription("Converting " + originalExt + " to " + targetExt + '\n');
        embed.appendDescription("Download timeout is " + downloadTimeout + " seconds\n");
        embed.appendDescription("Conversion timeout is " + conversionTimeout + " seconds");
        return embed.build();
    }

    private MessageEmbed createTimeoutEmbed(String message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Job timed out");
        embed.setDescription(message);
        return embed.build();
    }

    private MessageEmbed createCompletionEmbed(long millis) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Job done.");
        embed.setDescription("Completed in: " + millis + " ms");
        return embed.build();
    }

    private MessageEmbed createNoProgramEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Cannot run program");
        embed.setDescription("ffmpeg is likely not set as an environmental variable");
        return embed.build();
    }

    private MessageEmbed createEmptyFileEmbed(long millis, boolean status) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("File is empty");
        embed.setDescription("Process ended prematurely? (" + millis + "ms)\n");
        embed.appendDescription("Is process alive: " + status);
        return embed.build();
    }

    private static String normalize(String path) {
        return path.replace('\\', '/');
    }
}
