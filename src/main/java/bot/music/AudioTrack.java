package bot.music;

import no4j.core.Logger;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AudioTrack{
    protected static final Logger logger = Logger.getLogger("primary");

    protected static final double MILLIS_20 = 20;

    private byte[] songBytes;
    private int length;
    //private int offset;
    private double fragmentsOf20Ms;
    private boolean isOpus = false;

    private final String PATH;
    private final String NAME;
    private AudioInputStream audioStream;
    private volatile AudioFormat audioInfo;
    private boolean isBigEndian;
    private double lengthSeconds;

    protected AudioTrack(String path){
        this.PATH = path;
        this.NAME = Paths.get(PATH).getFileName().toString();

        try{
            String extFormat = audioFileFormat(PATH);
            AudioConversionResult result;
            switch (extFormat){
                case "mp3":
                    logger.info("-Attempting mp3 conversion-");
                    result = AudioConverter.convertMP3FileToRaw(PATH);
                    if(result == null) return;
                    songBytes = result.bytes;
                    audioStream = result.audioInputStream;
                    break;
                case "m4a":
                case "mp4":
                    result = AudioConverter.convertM4AToRaw(PATH);
                    if(result == null) return;
                    songBytes = result.bytes;
                    audioStream = result.audioInputStream;
                    break;
                default:
                    audioStream = AudioSystem.getAudioInputStream(new File(path));
                    songBytes = Files.readAllBytes(Paths.get(path));
                    songBytes = AudioConverter.removeWAVMetadata(songBytes);
            }
            result = AudioConverter.target48Hz(audioStream);
            songBytes = result.bytes != null ? result.bytes : songBytes;
            audioStream = result.audioInputStream;
        }catch (UnsupportedAudioFileException e){
            logger.stackTrace(PATH, e);
            return;
        }catch(IOException e){
            logger.stackTrace("IO error/Path doesn't exist", e);
            return;
        }

        initAudio();
        displayAudioInfo();
        closeAndNullifyAudioStream();
    }

    private void closeAndNullifyAudioStream(){
        try{
            audioStream.close();
        }catch (IOException e){
            logger.exception(e);
        }
        audioStream = null;
    }

    private void initAudio(){
        audioInfo = audioStream.getFormat();
        isBigEndian = audioInfo.isBigEndian();
        lengthSeconds = audioStream.getFrameLength() / audioInfo.getFrameRate();
        fragmentsOf20Ms = lengthSeconds * 1000 / MILLIS_20;
        length = (int) ((double) songBytes.length / fragmentsOf20Ms);
        fragmentsOf20Ms = (int) fragmentsOf20Ms;
        if (length % 2 == 1){
            length--;
            logger.debug("Made default buffer length even: " + length);
        }
        targetBigEndianness();
    }

    public void displayAudioInfo(){
        logger.debug(audioInfo + ", " + audioInfo.getChannels() + " channels, " + audioFileFormat(PATH) + " format");
        String length = String.format("Length(seconds): %.2f", lengthSeconds);
        logger.debug(length);
        logger.debug("Number of 20ms parts " + fragmentsOf20Ms);
        logger.debug("How many arr parts: " + length);
        logger.debug("Byte array size (audio file size): " + songBytes.length + " in MBs " + String.format("%.2f", (double) songBytes.length / 1048576L));
        logger.debug("Size of raw audio in input stream: " + audioStream.getFrameLength() * audioStream.getFormat().getFrameSize());
    }

    //flips endianness
    private void targetBigEndianness(){
        //getAudioInputStream() conversion may turn it big endian even if little endian was requested, which would lead to distorted audio,
        if(!isBigEndian){
            //modifies buffer's underlying array
            for (int i = 0, arrLen = songBytes.length -1; i < arrLen; i+=2){
                byte temp = songBytes[i];
                songBytes[i] = songBytes[i+1];
                songBytes[i+1] = temp;
            }
            isBigEndian = true;
            logger.debug("Each two consequent bytes were swapped to maintain big endian order");
        }
    }

    private String audioFileFormat(String path){
        int index = path.lastIndexOf('.') + 1;
        if(index<1) return "";
        return path.substring(index);
    }
    protected byte[] getSongBytes(){
        return songBytes;
    }
    protected int getBaseLength(){
        return length;
    }
    protected double fragmentsOf20Ms(){
        return fragmentsOf20Ms;
    }
    protected boolean isBigEndian(){
        return isBigEndian;
    }
    protected boolean isOpus(){
        return isOpus;
    }

    public double getLengthSeconds(){
        return lengthSeconds;
    }
    public String getTrackName(){
        return NAME;
    }

}
