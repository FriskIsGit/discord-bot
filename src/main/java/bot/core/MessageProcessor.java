package bot.core;

import bot.commands.Command;
import bot.commands.Commands;
import bot.utilities.*;

import bot.utilities.jda.Actions;
import bot.utilities.jda.MessageDeque;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
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
    public static final Button[] interactiveButtons = {
            Button.primary("clrsongs",  "Free songs from memory"),
            Button.primary("gc",        "Run GC"),
            Button.primary("refresh",   "Refresh")
    };
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
            Command command = Commands.get().command(commandName);
            if(command == null){
                return;
            }

            if(allSplit.length == 1){
                command.execute(commandName, message);
            }
            else{
                command.execute(commandName, message, Commands.shrink(allSplit, 1));
            }
        }
        else if(messageText.startsWith("$") && !messageText.startsWith("$$")){
            try{
                linuxRequest();
            }catch (Exception exc){exc.printStackTrace();}
        }
    }

    public void deleteRequestMessage(Message msgToDelete){
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
        Process proc = null;
        try{
            proc = run.exec(command);
            proc.waitFor(3, TimeUnit.SECONDS);
        }catch (IOException | InterruptedException ioException){
            ioException.printStackTrace();
            System.err.println("Execution error for command: " + command);
        }
        if (proc == null){
            System.err.println("Process is null");
            return;
        }
        InputStream inputStream = proc.getInputStream();

        //InputStream errorStream = procBuilder.getErrorStream();
        String stringedStream = StreamUtil.streamToString(inputStream);
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

    private boolean isAuthorAuthorized(){
        return Bot.AUTHORIZED_USERS.contains(message.getAuthor().getIdLong());
    }
}
