package bot.music;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AudioTrack{

    protected final static double MILLIS_20 = 20;

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
    private int audioBytesLength;


    protected AudioTrack(String path){
        this.PATH = path;
        this.NAME = Paths.get(PATH).getFileName().toString();

        try{
            String extFormat = audioFileFormat(PATH);
            if(extFormat.equals("mp3")){
                System.out.println("-Attempting mp3 conversion-");
                AudioConversionResult result = AudioConverter.convertMp3FileToRaw(PATH);
                if(result == null) return;
                songBytes = result.bytes;
                audioStream = result.audioInputStream;
            }else{
                audioStream = AudioSystem.getAudioInputStream(new File(path));
                songBytes = Files.readAllBytes(Paths.get(path));
                songBytes = AudioConverter.removeMetadata(songBytes);
            }
            AudioConversionResult result = AudioConverter.target48Hz(audioStream);
            songBytes = result.bytes != null ? result.bytes : songBytes;
            audioStream = result.audioInputStream;
        }catch (UnsupportedAudioFileException uafExc){
            System.err.println("Unsupported file format");
            System.out.println(PATH);
            return;
        }catch(IOException ioExc){
            System.err.println("IO error/Path doesn't exist");
            return;
        }

        audioBytesLength = songBytes.length;
        initAudio();
        displayAudioInfo();
        closeAndNullifyAudioStream();
    }

    private void closeAndNullifyAudioStream(){
        try{
            audioStream.close();
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
        audioStream = null;
    }

    private void initAudio(){
        audioInfo = audioStream.getFormat();
        isBigEndian = audioInfo.isBigEndian();
        lengthSeconds = audioStream.getFrameLength() / audioInfo.getFrameRate();
        fragmentsOf20Ms = lengthSeconds * 1000 / MILLIS_20;
        length = (int) ((double) audioBytesLength / fragmentsOf20Ms);
        fragmentsOf20Ms = (int) fragmentsOf20Ms;
        if (length % 2 == 1){
            length--;
            System.out.println("Made default buffer length even: " + length);
        }
        targetBigEndianness();
    }

    public void displayAudioInfo(){
        System.out.println(audioInfo + ", " + audioInfo.getChannels() + " channels, " + audioFileFormat(PATH) + " format");
        System.out.printf("Length(seconds): %.2f \n", lengthSeconds);
        System.out.println("Number of 20ms parts " + fragmentsOf20Ms);
        System.out.println("How many arr parts: " + length);
        System.out.println("Byte array size (audio file size): " + songBytes.length + " in MBs " + String.format("%.2f", (double) songBytes.length / 1048576L));
        System.out.println("Size of raw audio in input stream: " + audioStream.getFrameLength() * audioStream.getFormat().getFrameSize());
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
            System.out.println("Each two consequent bytes were swapped to maintain big endian order");
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
    public String getTrackName(){
        return NAME;
    }
    protected double fragmentsOf20Ms(){
        return fragmentsOf20Ms;
    }
    protected boolean isBigEndian(){
        return isBigEndian;
    }
    public boolean isOpus(){
        return isOpus;
    }

}
