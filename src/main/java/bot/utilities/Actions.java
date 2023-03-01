package bot.utilities;

import bot.deskort.Bot;
import bot.deskort.Commands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Actions{
    private final static int MB_8 = 8388608;
    private final JDA jdaInterface;
    private final Channels channels;
    private final Scanner scanner;
    public Actions(Channels channels){
        jdaInterface = Bot.getJDAInterface();
        this.channels = channels;
        scanner = new Scanner(System.in);
    }
    public void sendEmbed(TextChannel textChannel, MessageEmbed embed){
        if(embed == null){
            return;
        }
        textChannel.sendMessageEmbeds(embed).queue();
    }

    public boolean banUser(User user, Guild guildOfOrigin){
        try{
            guildOfOrigin.ban(user,0).queue();
        }catch (HierarchyException hierarchyExc){
            System.err.println(hierarchyExc.getMessage());
            return false;
        }
        return true;
    }
    public boolean unbanUser(User user, Guild guildOfOrigin){
        try{
            guildOfOrigin.unban(user).queue();
        }catch (HierarchyException hierarchyExc){
            System.err.println(hierarchyExc.getMessage());
            return false;
        }
        return true;
    }

    public void sendFile(TextChannel channel, File fileToSend){
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
            messageChannel(channel,"8MBs exceeded");
            return;
        }

        if(channel != null){
            System.out.println("Sending file..");
            channel.sendFile(bytes, fileToSend.getName()).queue();
        }
    }

    protected TextChannel findChannel(String msg){
        int index = msg.indexOf(' ') + 1;
        if(index == 0){
            return null;
        }
        String channelPartialName = msg.substring(index);
        return channels.getTextChannel(channelPartialName);
    }

    public void messageChannel(TextChannel txtChannel, String msgText){
        if(txtChannel == null) return;
        int msgLength = msgText.length();

        if(2000>=msgLength && msgLength>0 && !msgText.startsWith("\n")){
            txtChannel.sendMessage(msgText).queue();
        }
        else if(msgLength>2000){
            int parts = msgLength/2000 + 1;
            String [] messagesArr = new String[parts];
            for(int i = 0, offset = 0; i<messagesArr.length; i++, offset+=2000){
                messagesArr[i] = msgText.substring(offset, Math.min(msgLength, offset+2000));
            }

            for(String msg : messagesArr){
                txtChannel.sendMessage(msg).queue();
            }
        }
    }

    public void messageChannel(long channelId, String msgText){
        messageChannel(jdaInterface.getTextChannelById(channelId),msgText);
    }

    public void messageChannel(MessageChannel txtChannel, String msgText){
        messageChannel((TextChannel) txtChannel,msgText);
    }

    public void sendAsMessageBlock(TextChannel txtChannel, String msgText){
        if(txtChannel == null) return;
        int msgLength = msgText.length();

        if(1994>=msgLength && msgLength>0){
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
                messagesArr[i] = msgText.substring(offset, Math.min(msgLength, offset+1994));
            }

            for(String msg : messagesArr){
                txtChannel.sendMessage("```" + msg + "```").queue();
            }
        }
    }

    public void sendAsMessageBlock(long channelId, String msgText){
        sendAsMessageBlock(jdaInterface.getTextChannelById(channelId),msgText);
    }
    public void chatWithBot(TextChannel textChannel){
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
                    TextChannel nextChannel = findChannel(input);
                    if(nextChannel != null){
                        textChannel = nextChannel;
                        System.out.println("Moved to: " + nextChannel.getName());
                        inServer = true;
                    }
                    break;
                case "lc":
                    //server name required
                    Guild server = Bot.getServers().getServerIgnoreCase(args[1]);
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
            }
        }
        System.out.println("Exited chat permanently");
    }

    public void chatWithBot(String textChannelPartialName){
        chatWithBot(channels.getTextChannel(textChannelPartialName));
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
            userDM.sendFile(bytes, file.getName()).queue();
        }catch (IllegalArgumentException iaExc){
            System.err.println("Cannot send file to user");
        }
    }

    //TODO(never)
    public void purgeLastMessagesInChannel(TextChannel textChannel, int numberOfMessages){
        //implementation
    }
    public void purgeLastMessagesInChannel(String partialName, int numberOfMessages){
        purgeLastMessagesInChannel(channels.getTextChannel(partialName), numberOfMessages);
    }

}
