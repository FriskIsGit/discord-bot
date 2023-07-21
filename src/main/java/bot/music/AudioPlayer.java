package bot.music;

import bot.core.Bot;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.Nullable;
import bot.utilities.FileSeeker;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class AudioPlayer implements AudioSendHandler{
    private final static HashSet<String> SUPPORTED_FORMATS = new HashSet<>(Arrays.asList("wav","mp3","snd","aiff","aifc","au","m4a","mp4"));
    private static final HashMap<String, AudioTrack> fileNamesToSongs = new HashMap<>(32);

    private boolean looping = false;

    private AudioTrack audioTrack;
    private ByteBuffer audioBuffer;

    private int methodCalls = 0, offset = 0, length = 0;
    protected volatile double fragmentsOf20Ms = 0;
    protected volatile boolean isOpus;
    private volatile boolean playing = false;

    private final SongQueue songQueue;

    public AudioPlayer() {
        this.songQueue = new SongQueue();
    }

    public static boolean isExtensionSupported(String extension){
        return SUPPORTED_FORMATS.contains(extension);
    }

    public static long getSongsSizeInMemory(){
        long allBytes = 0;
        for (AudioTrack track : fileNamesToSongs.values()) {
            byte[] bytes = track.getSongBytes();
            if(bytes != null){
                allBytes += track.getSongBytes().length;
            }
        }
        return allBytes;
    }

    public static void clearAudioTracksFromMemory(){
        fileNamesToSongs.clear();
    }
    //returns new sendingHandler
    public static AudioPlayer addSendingHandlerIfNull(AudioManager audioManager){
        AudioPlayer sendingHandler;
        AudioPlayer currentHandler = (AudioPlayer) audioManager.getSendingHandler();
        if(currentHandler == null){
            sendingHandler = new AudioPlayer();
            System.out.println("-Setting up sending handler-");
            audioManager.setSendingHandler(sendingHandler);
            return sendingHandler;
        }
        return currentHandler;
    }

    public void setPlaying(boolean flag){
        if(audioBuffer == null){
            System.err.println("Audio buffer null");
            return;
        }
        this.playing = flag;
    }

    /**
     * invert looping value
     * @return new status
     */
    public boolean switchLooping(){
        looping ^= true;
        return looping;
    }

    public SongQueue getSongQueue(){
        return this.songQueue;
    }

    public AudioTrack getCurrentAudioTrack(){
        return this.audioTrack;
    }
    /**
     * @param trackName track key
     * @return false if track doesn't exist or track key empty
     */
    public boolean setAudioTrack(String trackName){
        if(trackName.isEmpty()){
            return false;
        }
        if(fileNamesToSongs.containsKey(trackName)){
            this.audioTrack = fileNamesToSongs.get(trackName);
        }else{
            FileSeeker fileSeeker = new FileSeeker(trackName, getAudioDirectory().getAbsolutePath());
            String containedPath = fileSeeker.findContainingPath();
            if(containedPath.isEmpty()){
                return false;
            }else{
                String absoluteFileName = Paths.get(containedPath).getFileName().toString();
                if(fileNamesToSongs.containsKey(absoluteFileName)){
                    this.audioTrack = fileNamesToSongs.get(absoluteFileName);
                }else{
                    this.audioTrack = new AudioTrack(containedPath);
                    fileNamesToSongs.put(absoluteFileName, audioTrack);
                }
            }
        }
        loadTrack();
        return true;
    }

    public File getAudioDirectory(){
        return Bot.getConfig().audioDirectory;
    }

    private void loadTrack(){
        byte [] bytes = audioTrack.getSongBytes();
        if(bytes == null){
            System.err.println("Underlying array null");
            return;
        }
        this.audioBuffer = ByteBuffer.wrap(bytes);
        //offset = audioTrack.getOffset();
        rewindTrack();
        fragmentsOf20Ms = audioTrack.fragmentsOf20Ms();
        length = audioTrack.getBaseLength();
        isOpus = audioTrack.isOpus();
    }

    public void rewindTrack(){
        offset = 0;
        methodCalls = 0;
    }

    @Override
    public boolean canProvide(){
        return playing;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio(){
        methodCalls++;

        if(methodCalls >= fragmentsOf20Ms-1){
            playing = false;
            System.out.println("Finished after " + methodCalls + " calls");
            if(looping){
                rewindTrack();
                playing = true;
            }else if(!songQueue.isEmpty()){
                boolean exists;
                do{
                    exists = setAudioTrack(songQueue.take());
                }while(!exists && !songQueue.isEmpty());
                if(exists){
                    playing = true;
                }
            }
        }

        audioBuffer.position(offset);
        audioBuffer.limit(offset+=length);

        return audioBuffer;
    }
    //encodes audio to opus if not Opus
    @Override
    public boolean isOpus(){
        return isOpus;
    }
}
