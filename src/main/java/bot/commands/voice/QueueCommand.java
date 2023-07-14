package bot.commands.voice;

import bot.commands.Command;
import bot.music.AudioPlayer;
import bot.music.SongQueue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;

public class QueueCommand extends Command{
    private static final Color niceGreen = new Color(34,139,34);
    private static final Color crimson = new Color(220,20,60);
    public QueueCommand(String... aliases){
        super(aliases);
        description = "Appends track to the end of song queue\n" +
                      "Displays queue if name isn't specified";
        usage = "queue `track_name`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        AudioPlayer audioPlayer = AudioPlayer.addSendingHandlerIfNull(audioManager);
        SongQueue songQueue = audioPlayer.getSongQueue();

        if(args.length == 0){
            if(songQueue.isEmpty()){
                actions.sendEmbed(message.getChannel(), createQueueEmbed(songQueue, true));
                return;
            }
            actions.sendEmbed(message.getChannel(), createQueueEmbed(songQueue, false));
            return;
        }
        songQueue.append(args[0]);
    }
    private MessageEmbed createQueueEmbed(SongQueue queue, boolean empty){
        String[] contents = queue.toStringArray();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(empty ? crimson : niceGreen);
        StringBuilder str = new StringBuilder();
        int position = 1;
        for(String song : contents){
            str.append(position++).append(". ").append(song).append('\n');
        }
        embed.addField(empty ? "Queue is empty" : "Songs in queue:", str.toString(), true);
        if(!empty){
            embed.addBlankField(true);
            embed.addField("Size: ", String.valueOf(contents.length), true);
        }
        return embed.build();
    }
}
