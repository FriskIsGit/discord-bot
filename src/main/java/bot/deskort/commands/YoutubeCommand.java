package bot.deskort.commands;

import bot.music.AudioPlayer;
import bot.music.youtube.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;

//uses youtube and youtube_lib packages
public class YoutubeCommand extends Command{
    private static final Color niceGreen = new Color(34,139,34);
    private static final Color crimson = new Color(220,20,60);

    public YoutubeCommand(String... aliases){
        super(aliases);
        description = "Retrieves information about youtube videos or downloads them\n";
        usage = "ytinfo `video_id`\n" +
                "ytviau `link` `format_number`\n" +
                "ytaudio `video_id` `format_number`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        AudioPlayer.addSendingHandlerIfNull(audioManager);

        if(args.length == 0){
            return;
        }
        YoutubeRequest youtubeRequest;
        StreamType type;
        switch(commandName){
            case "ytinfo":
                type = StreamType.INFO;
                break;
            case "ytviau":
                type = StreamType.VIDEO_AUDIO;
                break;
            case "ytaudio":
            case "ytau":
                type = StreamType.AUDIO;
                break;
            case "ytvideo":
            case "ytvi":
                type = StreamType.VIDEO;
                break;
            default:
                type = StreamType.NONE;
        }

        String id = Youtube.getVideoId(args[0]);
        if(type == StreamType.INFO){
            youtubeRequest = new YoutubeRequest(StreamType.INFO, id);
        }else{
            int formatNumber = Integer.parseInt(args[1]);
            youtubeRequest = new YoutubeRequest(type, id, formatNumber);
        }

        new Thread(() -> {
            MessageChannelUnion channel = message.getChannel();
            UserResponse response = UserRequest.executeRequest(youtubeRequest);
            if(response.success && response.hasFile){
                actions.sendFile(channel, response.fileAttachment);
            }
            sendResponseEmbed(channel, response);
        }).start();
    }
    private static void sendResponseEmbed(MessageChannelUnion channel, UserResponse response){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(response.success ? niceGreen : crimson);
        embed.setTitle(response.success ? "Success" : "Failure");
        if(response.message.length == 1){
            embed.addField("", response.message[0], true);
            actions.sendEmbed(channel, embed.build());
            return;
        }
        boolean first = true;
        for(String msg : response.message){
            if(first){
                embed.setDescription(msg);
                first = false;
                continue;
            }
            int len = msg.length();
            if(len > MessageEmbed.VALUE_MAX_LENGTH){
                embed.addField("", msg.substring(0, len/2), true);
                embed.addField("", msg.substring(len/2), true);
                continue;
            }
            embed.addField("", msg, false);
        }
        actions.sendEmbed(channel, embed.build());
    }
}
