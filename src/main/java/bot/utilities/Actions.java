package bot.utilities;

import bot.deskort.Bot;
import bot.deskort.commands.Commands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Actions{
    private final static int MB_8 = 8388608;
    private final JDA jdaInterface;
    private final Scanner scanner;
    public Actions(){
        jdaInterface = Bot.getJDAInterface();
        scanner = new Scanner(System.in);
    }
    public void sendEmbed(MessageChannelUnion channel, MessageEmbed embed){
        if(embed == null){
            return;
        }
        channel.sendMessageEmbeds(embed).queue();
    }

    public boolean banUser(User user, Guild guildOfOrigin){
        if(user == null){
            System.err.println("user == null");
            return false;
        }
        if(guildOfOrigin == null){
            System.err.println("guildOfOrigin == null");
            return false;
        }
        try{
            guildOfOrigin.ban(user, 0, TimeUnit.SECONDS).queue();
        }catch (HierarchyException hierarchyExc){
            System.err.println(hierarchyExc.getMessage());
            return false;
        }
        return true;
    }
    public boolean unbanUser(User user, Guild guildOfOrigin){
        if(user == null){
            System.err.println("user == null");
            return false;
        }
        if(guildOfOrigin == null){
            System.err.println("guildOfOrigin == null");
            return false;
        }
        try{
            guildOfOrigin.unban(user).queue();
        }catch (HierarchyException hierarchyExc){
            System.err.println(hierarchyExc.getMessage());
            return false;
        }
        return true;
    }

    public void sendFile(MessageChannel channel, File fileToSend){
        if(fileToSend == null){
            System.out.println("Given file was null");
            return;
        }
        byte [] bytes;
        try{
            bytes = Files.readAllBytes(fileToSend.toPath());
        }catch (IOException ioException){
            ioException.printStackTrace();
            return;
        }
        if(bytes.length > MB_8){
            channel.sendMessage("8MBs exceeded").queue();
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

    protected MessageChannel findChannel(String msg){
        int index = msg.indexOf(' ') + 1;
        if(index == 0){
            return null;
        }
        String channelPartialName = msg.substring(index);
        return getMessageChannel(channelPartialName);
    }

    public MessageChannel getMessageChannel(String partialName){
        List<Guild> serversList = jdaInterface.getGuilds();
        for(Guild guild : serversList){
            List<GuildChannel> listOfChannels = guild.getChannels();
            for(GuildChannel channel : listOfChannels){
                if(channel.getName().contains(partialName)){
                    if(channel instanceof MessageChannel){
                        return (MessageChannel) channel;
                    }
                }
            }
        }
        return null;
    }

    public TextChannel getTextChannel(String partialName){
        List<TextChannel> textChannels = jdaInterface.getTextChannels();
        for (TextChannel textChannel : textChannels){
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
        if(txtChannel == null) return;
        int msgLength = msgText.length();

        if(2000>=msgLength && msgLength>0 && !msgText.startsWith("\n")){
            txtChannel.sendMessage(msgText).queue();
        }
        else if(msgLength>2000){
            int parts = msgLength/2000 + 1;
            String[] messagesArr = new String[parts];
            for(int i = 0, offset = 0; i<messagesArr.length; i++, offset+=2000){
                int endIndex = Math.min(msgLength, offset+2000);
                messagesArr[i] = msgText.substring(offset, endIndex);
            }

            for(String msg : messagesArr){
                txtChannel.sendMessage(msg).queue();
            }
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

        if(1994 >= msgLength && msgLength>0){
            try{
                txtChannel.sendMessage("```" + msgText + "```").queue();
            }catch (InsufficientPermissionException insufficientPermExc){
                System.err.println("Lacking permission MESSAGE_SEND");
            }
        }
        else if(msgLength>2000){
            int parts = msgLength/2000 + 1;
            String [] messagesArr = new String[parts];
            for(int i = 0, offset = 0; i<messagesArr.length; i++, offset+=1994){
                int endIndex = Math.min(msgLength, offset+1994);
                messagesArr[i] = msgText.substring(offset, endIndex);
            }

            for(String msg : messagesArr){
                txtChannel.sendMessage("```" + msg + "```").queue();
            }
        }
    }

    public void sendAsMessageBlock(long channelId, String msgText){
        sendAsMessageBlock(jdaInterface.getTextChannelById(channelId),msgText);
    }
    public void chatWithBot(MessageChannel textChannel){
        if(textChannel == null)
            return;

        boolean inServer = true;
        long dmId = 0;
        String input;

        outer:
        while(true) {
            input = scanner.nextLine();
            if(!input.startsWith(Bot.PREFIX)){
                if(inServer){
                    messageChannel(textChannel, input);
                }else{
                    this.messageUser(dmId, input);
                }
                continue;
            }
            String[] args = Commands.doubleTermSplit(input, Bot.PREFIX_OFFSET);
            switch (args[0]){
                case "cc":
                    MessageChannel nextChannel = findChannel(input);
                    if(nextChannel != null){
                        textChannel = nextChannel;
                        System.out.println("Moved to: " + nextChannel.getName());
                        inServer = true;
                    }
                    break;
                case "lc":
                    //server name required
                    Guild server = getServerIgnoreCase(args[1]);
                    if (server == null){
                        continue;
                    }
                    System.out.println(server.getTextChannels());
                    break;
                case "dm":
                    //user id required
                    try{
                        dmId = Long.parseLong(args[1]);
                        System.out.println("Switched to dm, id: " + dmId);
                        inServer = false;
                    }catch (NumberFormatException numFormatExc){
                        System.out.println("Failed to parse id");
                    }
                    break;
                case "file":
                    //file path
                    File file = new File(args[1]);
                    if(inServer){
                        sendFile(textChannel, file);
                    }else{
                        sendFileToUser(dmId, file);
                    }
                    break;
                case "exit":
                    //exit chat
                    break outer;
                case "where": {
                    if (inServer)
                        System.out.println("Current channel: " + textChannel);
                    else System.out.println("In dm: " + dmId);
                }
                default:
                    String[] remainingArgs = Commands.splitIntoTerms(args[1]);
                    try{
                        Commands.get().command(args[0]).executeUnrestricted(args[0], remainingArgs);
                    }catch (Exception exc){
                        System.err.println(exc.getMessage());
                    }
            }
        }
        System.out.println("Exited chat permanently");
    }

    public void chatWithBot(String textChannelPartialName){
        chatWithBot(getTextChannel(textChannelPartialName));
    }

    public void messageUser(long userId, String messageContent){
        User user = jdaInterface.getUserById(userId);
        //if not cached - retrieve
        if(user == null){
            user = jdaInterface.retrieveUserById(userId).complete();
        }
        PrivateChannel userDM = user.openPrivateChannel().complete();
        try{
            userDM.sendMessage(messageContent).queue();
        }catch (Exception exc){
            System.err.println("Cannot message user: " + exc);
        }
    }

    public void sendFileToUser(long userId, File file){
        User user = jdaInterface.getUserById(userId);
        //if not cached - retrieve
        if(user == null){
            user = jdaInterface.retrieveUserById(userId).complete();
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
                System.out.println("IOException occurred on file upload");
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
