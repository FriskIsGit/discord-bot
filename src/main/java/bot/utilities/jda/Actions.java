package bot.utilities.jda;

import bot.deskort.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Actions{
    private final static int MEGABYTES_25 = 26214400;
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
            guildOfOrigin.ban(user, 0, TimeUnit.SECONDS).queue();
        }catch (HierarchyException e){
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
            guildOfOrigin.unban(user).queue();
        }catch (HierarchyException e){
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
            guildOfOrigin.kick(user).reason(reason).queue();
        }catch (HierarchyException e){
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

        if(2000 >= length){
            CompletableFuture<Message> msgPromise = txtChannel.sendMessage(msgText).submit();
            messageCache.add(msgPromise);
            return;
        }
        int parts = length / 2000 + 1;
        String[] messagesArr = new String[parts];
        for(int i = 0, offset = 0; i < messagesArr.length; i++, offset+=2000){
            int endIndex = Math.min(length, offset+2000);
            messagesArr[i] = msgText.substring(offset, endIndex);
        }

        for(String msg : messagesArr){
            CompletableFuture<Message> msgPromise = txtChannel.sendMessage(msg).submit();
            messageCache.add(msgPromise);
        }
    }

    public void messageChannel(long channelId, String msgText){
        messageChannel(jdaInterface.getTextChannelById(channelId), msgText);
    }

    public void messageChannel(MessageChannel txtChannel, String msgText){
        messageChannel((TextChannel) txtChannel, msgText);
    }

    public void sendAsMessageBlock(MessageChannel txtChannel, String msgText){
        if(txtChannel == null)
            return;
        int msgLength = msgText.length();

        if(1994 >= msgLength && msgLength > 0){
            CompletableFuture<Message> msgPromise = txtChannel.sendMessage("```" + msgText + "```").submit();
            messageCache.add(msgPromise);
        }
        else if(msgLength>2000){
            int parts = msgLength/2000 + 1;
            String[] messagesArr = new String[parts];
            for(int i = 0, offset = 0; i<messagesArr.length; i++, offset+=1994){
                int endIndex = Math.min(msgLength, offset+1994);
                messagesArr[i] = msgText.substring(offset, endIndex);
            }

            for(String msg : messagesArr){
                CompletableFuture<Message> msgPromise = txtChannel.sendMessage("```" + msg + "```").submit();
                messageCache.add(msgPromise);
            }
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
