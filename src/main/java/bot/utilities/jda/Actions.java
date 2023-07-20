package bot.utilities.jda;

import bot.core.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Actions{
    private static final int MEGABYTES_25 = 26214400;
    private static final int MAX_CONTENT_LENGTH = Message.MAX_CONTENT_LENGTH;
    private static final int SPLIT_LIMIT = 50;
    private final List<CompletableFuture<Message>> messageCache = new ArrayList<>(64);
    private final JDA jdaInterface;

    public Actions(){
        jdaInterface = Bot.getJDAInterface();
    }
    public void sendEmbed(MessageChannelUnion channel, MessageEmbed embed){
        if(embed == null){
            return;
        }
        CompletableFuture<Message> msgPromise = channel.sendMessageEmbeds(embed).submit();
        messageCache.add(msgPromise);
    }

    public boolean banUser(User user, Guild guildOfOrigin){
        if(user == null || guildOfOrigin == null){
            return false;
        }
        try{
            guildOfOrigin.ban(user, 0, TimeUnit.SECONDS).complete();
        }catch (InsufficientPermissionException | ErrorResponseException | HierarchyException e){
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
    public boolean unbanUser(User user, Guild guildOfOrigin){
        if(user == null || guildOfOrigin == null){
            return false;
        }
        try{
            guildOfOrigin.unban(user).complete();
        }catch (InsufficientPermissionException | HierarchyException e){
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
    public boolean kickUser(User user, Guild guildOfOrigin, String reason){
        if(user == null || guildOfOrigin == null){
            return false;
        }
        try{
            guildOfOrigin.kick(user).reason(reason).complete();
        }catch (InsufficientPermissionException | ErrorResponseException | HierarchyException e){
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public void sendFile(MessageChannel channel, File fileToSend){
        if(fileToSend == null){
            System.err.println("Given file was null");
            return;
        }
        if(!fileToSend.exists()){
            System.err.println("Given file doesn't exist");
            return;
        }
        byte[] bytes;
        try{
            bytes = Files.readAllBytes(fileToSend.toPath());
        }catch (IOException ioException){
            ioException.printStackTrace();
            return;
        }
        if(bytes.length > MEGABYTES_25){
            channel.sendMessage("25MBs exceeded").queue();
            return;
        }

        if(channel != null){
            try (FileUpload upload = FileUpload.fromData(bytes, fileToSend.getName())){
                channel.sendFiles(upload).queue();
                System.out.println("Sending file..");
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    public TextChannel getTextChannel(String partialName){
        List<TextChannel> textChannels = jdaInterface.getTextChannels();
        for (TextChannel textChannel : textChannels){
            //if an exact match exists match to it first
            if (textChannel.getName().equals(partialName)){
                return textChannel;
            }
        }
        for (TextChannel textChannel : textChannels){
            //if an exact match exists match to it first
            if (textChannel.getName().contains(partialName)){
                return textChannel;
            }
        }
        return null;
    }

    public Guild getGuildById(long id){
        List<Guild> guilds = jdaInterface.getGuilds();
        for (Guild guild : guilds){
            if(guild.getIdLong() == id){
                return guild;
            }
        }
        return null;
    }

    public void messageChannel(TextChannel txtChannel, String msgText){
        if(txtChannel == null || msgText.isEmpty())
            return;
        int length = msgText.length();

        if(MAX_CONTENT_LENGTH >= length){
            CompletableFuture<Message> msgPromise = txtChannel.sendMessage(msgText).submit();
            messageCache.add(msgPromise);
            return;
        }

        List<String> messageParts = smartPartition(msgText, MAX_CONTENT_LENGTH);
        for(String msg : messageParts){
            CompletableFuture<Message> msgPromise = txtChannel.sendMessage(msg).submit();
            messageCache.add(msgPromise);
        }
    }

    private List<String> smartPartition(String msgText, int SIZE_PER_MSG){
        int length = msgText.length();
        int partsEst = length / SIZE_PER_MSG + 1;
        List<String> messageParts = new ArrayList<>(partsEst);
        int lastSplit = 0;
        int counter = 0;
        while(counter++ < SPLIT_LIMIT){
            int widestSplit = lastSplit + SIZE_PER_MSG;
            if(widestSplit >= length){
                String last = msgText.substring(lastSplit, length);
                messageParts.add(last);
                break;
            }

            int nextSplit = msgText.lastIndexOf('\n', widestSplit);
            if(nextSplit == lastSplit){
                //accounts for - not found case
                String part = msgText.substring(lastSplit, widestSplit);
                messageParts.add(part);
                lastSplit = widestSplit;
                continue;
            }
            String part = msgText.substring(lastSplit, nextSplit);
            messageParts.add(part);
            lastSplit = nextSplit;
        }
        return messageParts;
    }

    public void messageChannel(long channelId, String msgText){
        messageChannel(jdaInterface.getTextChannelById(channelId), msgText);
    }

    public void messageChannel(MessageChannel txtChannel, String msgText){
        messageChannel((TextChannel) txtChannel, msgText);
    }

    public void sendAsMessageBlock(MessageChannel txtChannel, String msgText){
        if(txtChannel == null || msgText.isEmpty())
            return;
        int msgLength = msgText.length();

        if(MAX_CONTENT_LENGTH-6 >= msgLength){
            CompletableFuture<Message> msgPromise = txtChannel.sendMessage("```" + msgText + "```").submit();
            messageCache.add(msgPromise);
            return;
        }

        List<String> messageParts = smartPartition(msgText, MAX_CONTENT_LENGTH - 6);
        for(String msg : messageParts){
            CompletableFuture<Message> msgPromise = txtChannel.sendMessage("```" + msg + "```").submit();
            messageCache.add(msgPromise);
        }
    }

    public void sendAsMessageBlock(long channelId, String msgText){
        sendAsMessageBlock(jdaInterface.getTextChannelById(channelId), msgText);
    }

    public void messageUser(long userId, String messageContent){
        User user = jdaInterface.getUserById(userId);
        //if not cached - retrieve
        if(user == null){
            try{
                user = jdaInterface.retrieveUserById(userId).complete();
            }catch (RuntimeException unknownUser){
                System.err.println("UNKNOWN USER");
                return;
            }
        }
        PrivateChannel userDM = user.openPrivateChannel().complete();
        userDM.sendMessage(messageContent).queue();
    }

    public void clearQueuedMessages(){
        for (int i = 0; i < messageCache.size(); i++){
            CompletableFuture<Message> promise = messageCache.get(i);
            if (!promise.isDone()){
                //useless boolean value
                promise.cancel(false);
            }
            messageCache.remove(i--);
        }
    }

    public void sendFileToUser(long userId, File file){
        User user = jdaInterface.getUserById(userId);
        //if not cached - retrieve
        if(user == null){
            try{
                user = jdaInterface.retrieveUserById(userId).complete();
            }catch (RuntimeException unknownUser){
                System.err.println("UNKNOWN USER");
                return;
            }
        }
        PrivateChannel userDM = user.openPrivateChannel().complete();
        try{
            byte[] bytes;
            try{
                bytes = Files.readAllBytes(file.toPath());
            }catch (IOException ioException){
                return;
            }
            try (FileUpload upload = FileUpload.fromData(bytes, file.getName())){
                userDM.sendFiles(upload).queue();
                System.out.println("Sending file..");
            }catch (IOException e){
                System.err.println("IOException occurred on file upload");
            }
        }catch (IllegalArgumentException iaExc){
            System.err.println("Cannot send file to user");
        }
    }
    public Guild getServerIgnoreCase(String partialName){
        partialName = partialName.toLowerCase(Locale.ROOT);
        List<Guild> servers = jdaInterface.getGuilds();
        for (Guild server : servers){
            if (server.getName().toLowerCase(Locale.ROOT).contains(partialName)){
                return server;
            }
        }
        return null;
    }

    public List<String> getServerNames(){
        List<Guild> guilds = jdaInterface.getGuilds();
        int numberOfGuilds = guilds.size();
        List<String> serverNames = new ArrayList<>(numberOfGuilds);
        for (Guild guild : guilds){
            serverNames.add(guild.getName());
        }
        return serverNames;
    }

    public List<Long> getServerIds(){
        List<Guild> guilds = jdaInterface.getGuilds();
        List<Long> ids = new ArrayList<>(guilds.size());
        for (Guild guild : guilds){
            ids.add(guild.getIdLong());
            System.out.println(guild.getName());
        }
        return ids;
    }
}
