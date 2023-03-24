package bot.deskort.commands.voice;

import bot.deskort.commands.Command;
import bot.deskort.commands.Commands;
import bot.music.AudioPlayer;
import bot.music.SongQueue;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class QueueCommand extends Command{
    public QueueCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        AudioPlayer audioPlayer = AudioPlayer.addSendingHandlerIfNull(audioManager);

        String songName = Commands.mergeTerms(args);
        if(songName.isEmpty()){
            //display queue
            SongQueue songQueue = audioPlayer.getSongQueue();
            if(songQueue.isEmpty()){
                actions.messageChannel(message.getChannel(), "Queue is empty");
            }else{
                actions.messageChannel(message.getChannel(), songQueue.toString());
            }

        }else{
            //add to queue
            audioPlayer.getSongQueue().append(songName);
        }
    }
}
