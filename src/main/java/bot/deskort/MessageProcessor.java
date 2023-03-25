package bot.deskort;

import bot.deskort.commands.Command;
import bot.deskort.commands.Commands;
import bot.utilities.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.*;

import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

//singleton
public class MessageProcessor{
    private static MessageProcessor messageProcessor;
    public static final List<Button> interactiveButtons = new ArrayList<>(Arrays.asList(
            Button.primary("clrsongs",  "Free songs from memory"),
            Button.primary("gc",        "Run GC"),
            Button.primary("refresh",   "Refresh")
    ));
    private final Actions actions;

    public final HashMap<Long, MessageDeque> channelIdsToMessageDeques = new HashMap<>();

    private MessageReceivedEvent message;
    private String messageText;
    private long channelId;

    private final Runtime run = Runtime.getRuntime();
    private final String OS = System.getProperty("os.name");

    private MessageProcessor(){
        actions = Bot.getActions();
        Command.initializeStaticMembers();
    }

    public static MessageProcessor get(){
        if(messageProcessor == null){
            messageProcessor = new MessageProcessor();
        }
        return messageProcessor;
    }

    private void logMessage(){
        if(channelIdsToMessageDeques.containsKey(channelId)){
            channelIdsToMessageDeques.get(channelId).add(message);
        }else{
            MessageDeque deq = new MessageDeque(Bot.getConfig().maxDequeSize);
            deq.add(message);
            channelIdsToMessageDeques.put(channelId, deq);
        }
    }

    public void processMessage(MessageReceivedEvent message, long channelId){
        this.message = message;
        messageText = message.getMessage().getContentRaw();
        this.channelId = channelId;
        logMessage();
        dispatchCommand();
    }

    private void dispatchCommand(){
        if(messageText.startsWith(Bot.PREFIX)){
            final String[] allSplit = Commands.splitIntoTerms(messageText, Bot.PREFIX_OFFSET);
            String commandName = allSplit[0].toLowerCase();
            if(commandName.isEmpty()){
                return;
            }
            Command command = bot.deskort.commands.Commands.get().command(commandName);
            if(command == null){
                return;
            }

            if(allSplit.length == 1){
                command.execute(commandName, message);
            }
            else{
                String[] args = new String[allSplit.length - 1];
                System.arraycopy(allSplit, 1, args, 0, args.length);
                command.execute(commandName, message, args);
            }
        }
        else if(messageText.startsWith("$") && !messageText.startsWith("$$")){
            try{
                linuxRequest();
            }catch (Exception exc){exc.printStackTrace();}
        }
    }

    final String HELP_MESSAGE =
            " [Available commands]\n" +
            " purge <amount> - channel based purge (each channel has its own deque, incorporates retrieving history when needed)\n" +
            " warp <voiceChannelName> - warps you to a voice channel (provided you're in one already)\n" +
            " join <partialName> - if left blank bot will attempt to join message author\n" +
            " leave - disconnects bot from channel\n" +
            " play <track> - makes bot play its 48Khz 16bit stereo 2channel 4bytes/frame BIG.ENDIAN PCM Signed opus encoded audio\n" +
            " stop - ends playback of the current song\n" +
            " tracks - displays all available tracks, some may be distorted\n" +
            " queue <track> - enqueues specified track, if name was not provided - displays the queue\n" +
            " skip - consumes the first song in queue and loads it\n" +
            " loop - self explanatory\n" +
            " sha <text> - oen of many hashing algorithms (e.g. md5, sha256)\n" +
            " mempanel - display memory management panel\n" +
            " uptime\n" +
            " len <text>\n - display text length" +
            " cat <number>\n - display HTTP status codes" +
            " [Youtube Commands] <format_number> index at which it appears counting from the top (0-indexed)\n" +
            " ytinfo <videoID/link> retrieves information about the youtube video, displaying available formats\n" +
            " ytaudio <videoID/link> <format_number> retrieves audio file in specified format\n" +
            " ytvideo <videoID/link> <format_number> retrieves video file in specified format\n" +
            " ytviau <videoID/link> <format_number> retrieves video with audio in specified format\n";

    public void deleteRequestMessage(){
        Message msgToDelete = message.getMessage();
        msgToDelete.delete().queue();
        channelIdsToMessageDeques.get(msgToDelete.getChannel().getIdLong()).removeLast();
    }

    /**
     * @param channel - where the purge occurs
     * @param amount - excluding the purge request message
     * @returns id of the oldest message as a reference point
     **/
    public String popAndPurgeLastMessages(MessageChannel channel, int amount){
        List<Message> list = channelIdsToMessageDeques.get(channelId).toList(amount);
        int lastIndex = list.size()-1;
        String oldestMessageId = list.get(lastIndex).getId();
        List<CompletableFuture<Void>> completableFutureList = channel.purgeMessages(list);
        completeInFuture(completableFutureList);
        return oldestMessageId;
    }
    public static void completeInFuture(List<CompletableFuture<Void>> futures){
        futures.forEach(future -> future.completeExceptionally(new Throwable("Insufficient permissions to purge?")));
    }

    protected void linuxRequest(){
        if(OS.toLowerCase(Locale.ENGLISH).startsWith("win")){
            actions.messageChannel(message.getChannel(),"Host running windows");
            return;
        }
        if(!isAuthorAuthorized()){
            return;
        }
        String command = messageText.substring(messageText.indexOf("$") + 1);
        Process procBuilder = null;
        try{
            procBuilder = run.exec(command);
            procBuilder.waitFor(3, TimeUnit.SECONDS);
        }catch (IOException | InterruptedException ioException){
            ioException.printStackTrace();
            System.err.println("Execution error for command: " + command);
        }
        if (procBuilder == null){
            System.err.println("Process is null");
            return;
        }
        InputStream inputStream = procBuilder.getInputStream();

        //InputStream errorStream = procBuilder.getErrorStream();
        String stringedStream = streamToString(50_000, inputStream);
        if (stringedStream != null){
            actions.sendAsMessageBlock(message.getChannel(), stringedStream);
            try{
                inputStream.close();
            }catch (IOException ioException){
                ioException.printStackTrace();
            }
            inputStream = null;
            stringedStream = null;
        }
        System.out.println("Finished request");
    }

    public static String streamToString(int initialSize, InputStream inputStream){
        String output;
        byte [] buffer = new byte[initialSize];
        try {
            int offset = 0;
            while (inputStream.available() != 0) {
                int available = inputStream.available();
                if(available + offset < buffer.length){
                    int currentRead = inputStream.read(buffer, offset, available);
                    offset += currentRead;
                }else{
                    byte [] tempBuffer = new byte[buffer.length<<1];
                    System.arraycopy(buffer,0,tempBuffer,0,offset);
                    buffer = tempBuffer;
                    tempBuffer = null;
                }
            }
            output = bytesToStr(buffer,offset);
            return output;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("Stream closed?");
        }
        return null;
    }
    private static String bytesToStr(byte [] bytes, int offset){
        char [] charArr = new char[offset];
        for(int i = 0; i<offset; i++){
            charArr[i] = (char)bytes[i];
        }
        return new String(charArr);
    }

    private boolean isAuthorAuthorized(){
        return Bot.AUTHORIZED_USERS.contains(message.getAuthor().getIdLong());
    }
}
