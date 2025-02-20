package bot.utilities.jda;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.managers.AudioManager;
import no4j.core.Logger;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// Functionality: disconnect the bot if it remains alone in a voice channel for too long, works per server
// Purpose: saving resources
public class LeaverTimer{
    private static final Logger logger = Logger.getLogger("primary");

    private boolean scheduled = false;
    private Timer timer;
    private TimerTask task;
    private final AudioManager guildAudio;
    private final long DELAY_MS = 20000;

    //audio manager of guild to which the timer belongs to
    public LeaverTimer(AudioManager guildAudio){
        this.guildAudio = guildAudio;
    }
    private TimerTask createNewTask(){
        return new TimerTask(){
            @Override
            public void run(){
                if(guildAudio.isConnected()){
                    @SuppressWarnings("all")
                    List<Member> connectedMembers = guildAudio.getConnectedChannel().getMembers();
                    if(connectedMembers.size() > 1){
                        return;
                    }
                    guildAudio.closeAudioConnection();
                    scheduled = false;
                    logger.info("Closed audio connection due to inactivity at " + Date.from(Instant.now()));
                }
            }
        };
    }
    public void schedule(){
        if(scheduled){
            cancel();
        }
        timer = new Timer();
        task = createNewTask();
        timer.schedule(task, DELAY_MS);
        scheduled = true;
    }
    public void cancel(){
        task.cancel();
        timer.cancel();
        scheduled = false;
    }
    public boolean isScheduled(){
        return scheduled;
    }
}
