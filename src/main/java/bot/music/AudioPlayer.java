package bot.music;

import bot.deskort.Commands;
import bot.music.youtube.SongQueue;
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

    private final static HashSet<String> SUPPORTED_FORMATS = new HashSet<>(Arrays.asList("wav","mp3","snd","aiff","aifc","au","mp4 (very few containers)"));
    private final static String HOME_DIR = System.getProperty("user.home");
    public static File AUDIO_FILES_DIR = new File(HOME_DIR);
    private boolean looping = false;

    private static final int NUM_OF_SONGS = 32;
    private static final HashMap<String, AudioTrack> fileNamesToSongs = new HashMap<>(NUM_OF_SONGS);

    private AudioTrack audioTrack;
    private ByteBuffer audioBuffer;

    private int methodCalls = 0;
    protected int offset = 0;
    protected int length = 0;
    volatile protected double fragmentsOf20Ms = 0;
    volatile protected boolean isOpus;

    protected AudioManager audioManager;
    protected SongQueue songQueue;
    volatile private boolean playing = false;

    public AudioPlayer(AudioManager audioManager) {
        this.audioManager = audioManager;
        this.songQueue = new SongQueue();
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
            FileSeeker fileSeeker = new FileSeeker(trackName, AUDIO_FILES_DIR.getAbsolutePath());
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
