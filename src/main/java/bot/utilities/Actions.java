package bot.utilities;

import bot.deskort.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public void chatWithBot(TextChannel textChannel){
        if(textChannel == null) return;
        String input;
        do {
            input = scanner.nextLine();
            if(isChannelChangeRequested(input)){
                TextChannel nextChannel = findChannel(input);
                if(nextChannel != null){
                    textChannel = nextChannel;
                    System.out.println("Moved to: " + nextChannel.getName());
                }
                continue;
            }else if(isListOfChannelsRequested(input)){
                String serverPartialName = input.substring(4);
                Guild server = Bot.getServers().getServerIgnoreCase(serverPartialName);
                if(server == null){
                    continue;
                }
                System.out.println(server.getTextChannels());
                continue;
            }
            /*else if (input.contains("[file:")){
                continue;
            }*/
            messageChannel(textChannel,input);

        }while(!input.equals("exit"));
    }

    private boolean isListOfChannelsRequested(String input){
        return (input.startsWith(">lc") || input.startsWith(">ls")) && input.length() > 4;
    }

    public void chatWithBot(String textChannelPartialName){
        chatWithBot(channels.getTextChannel(textChannelPartialName));
    }
    public void chatWithBot(long channelId){
        chatWithBot(jdaInterface.getTextChannelById(channelId));
    }
    public void chatWithBot(MessageChannel channel){
        chatWithBot((TextChannel) channel);
    }

    public void sendFile(long channelId, String path){
        byte [] bytes = null;
        try{
            bytes = Files.readAllBytes(Paths.get(path));
        }catch (IOException ioException){
            ioException.printStackTrace();
        }

        TextChannel channel = jdaInterface.getTextChannelById(channelId);
        if(bytes != null && channel != null){
            int lastSlash = path.lastIndexOf('/');
            if(lastSlash == - 1){
                lastSlash = path.lastIndexOf('\\');
            }
            channel.sendFile(bytes,path.substring(lastSlash+1)).queue();
        }
    }

    public void sendFile(long channelId, File fileToSend){
        byte [] bytes = null;
        try{
            bytes = Files.readAllBytes(fileToSend.toPath());
        }catch (IOException ioException){
            ioException.printStackTrace();
            return;
        }
        if(bytes.length > MB_8){
            messageChannel(channelId,"8MBs exceeded");
            return;
        }

        TextChannel channel = jdaInterface.getTextChannelById(channelId);
        if(channel != null){
            channel.sendFile(bytes, fileToSend.getName()).queue();
        }
    }

    public void sendFile(String partialChannelName, String filePath){
        byte [] bytes = null;
        try{
            bytes = Files.readAllBytes(Paths.get(filePath));
        }catch (IOException ioException){
            ioException.printStackTrace();
            return;
        }

        TextChannel channel = channels.getTextChannel(partialChannelName);
        if(channel == null){
            return;
        }
        if(bytes.length > MB_8){
            messageChannel(channel.getIdLong(),"8MBs exceeded");
        }

        int lastSlash = filePath.lastIndexOf('/');
        if(lastSlash == - 1){
            lastSlash = filePath.lastIndexOf('\\');
        }
        channel.sendFile(bytes,filePath.substring(lastSlash+1)).queue();
    }

    public void sendFile(long channelId, String path, String fileName){
        byte [] bytes = null;
        try{
            bytes = Files.readAllBytes(Paths.get(path));
        }catch (IOException ioException){
            ioException.printStackTrace();
        }

        TextChannel channel = jdaInterface.getTextChannelById(channelId);
        if(bytes != null && channel != null){
            channel.sendFile(bytes,fileName).queue();
        }
    }

    protected TextChannel findChannel(String msg){
        int index = msg.indexOf(' ') + 1;
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
            txtChannel.sendMessage("```" + msgText + "```").queue();
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


    protected boolean isChannelChangeRequested(String msg){
        return (msg.startsWith(">cc") || msg.startsWith(">cd")) && msg.length() > 4;
    }

    public void joinVoiceChannel(long channelId){
        VoiceChannel vc = jdaInterface.getVoiceChannelById(channelId);
        AudioManager audioManager = Bot.getServers().getServers().get(0).getAudioManager();
        audioManager.openAudioConnection(vc);
    }
    public void joinVoiceChannel(String partialServerName, String partialChannelName){
        String partialServerNameLower = partialServerName.toLowerCase(Locale.ROOT);
        Guild destGuild = null;
        List<Guild> servers = Bot.getServers().getServers();
        for(Guild server : servers){
            if(server.getName().toLowerCase(Locale.ROOT).contains(partialServerNameLower)){
                destGuild = server;
            }
        }
        if(destGuild == null) return;
        AudioManager audioManager = destGuild.getAudioManager();
        VoiceChannel vc = channels.getVoiceChannelIgnoreCase(partialChannelName);
        audioManager.openAudioConnection(vc);
    }
    public void purgeLastMessagesInChannel(TextChannel textChannel, int numberOfMessages){
        //implementation
    }

    public void purgeLastMessagesInChannel(String partialName, int numberOfMessages){
        purgeLastMessagesInChannel(channels.getTextChannel(partialName), numberOfMessages);
    }





}
