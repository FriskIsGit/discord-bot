package bot.deskort.commands;

import bot.music.AudioPlayer;
import bot.music.youtube.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

//uses youtube and youtube_lib packages
public class YoutubeCommand extends Command{
    private final Youtube youtube = new Youtube();
    public YoutubeCommand(String... aliases){
        super(aliases);
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
            UserRequest userRequest = new UserRequest(youtube);
            UserResponse response = userRequest.executeRequest(youtubeRequest);
            if(response.success && response.hasFile){
                actions.sendFile(message.getChannel(), response.fileAttachment);
            }else{
                actions.messageChannel(message.getChannel(), "Request failed, reason: " + response.message);
            }
        }).start();
    }
}
