package bot.music;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AudioConverter{
    public final static short WAVE_OFFSET = 44;
    public final static float SAMPLE_RATE_OFFSET = 500f;
    public final static float TARGET_SAMPLE_RATE = 48000f;
    public final static AudioFormat TARGET_FORMAT = new AudioFormat(TARGET_SAMPLE_RATE, 16, 2, true,true);
    private AudioConverter(){
    }

    public static AudioConversionResult convertMp3FileToRaw(String mp3FilePath){
        //audioFileLength / (frameSize * frameRate)
        MP3Decoder mp3Decoder;
        MP3Sound mp3Sound;
        try{
            mp3Sound = new MP3Sound(mp3FilePath);
            mp3Decoder = new MP3Decoder(mp3FilePath);
        }catch (IOException ioException){
            ioException.printStackTrace();
            return null;
        }
        int decodedSamples = mp3Decoder.decodeEntireMP3();
        System.out.println("Decoded samples: " + decodedSamples);
        System.out.println("ByteArrInputStream size: " + mp3Decoder.getByteArrayOutputStream().size());
        byte[] songBytes = mp3Decoder.getStreamBytes();
        AudioInputStream audioStream = new AudioInputStream(mp3Decoder.getByteArrInStream(), mp3Sound.getSound().getAudioFormat(), decodedSamples/2);
        return new AudioConversionResult(songBytes, audioStream);
    }

    //inputStream = new AudioInputStream(new ByteArrayInputStream(new byte[]{}), AudioTrack.TARGET_FORMAT, length);
    public static AudioConversionResult target48Hz(AudioInputStream audioStream){
        AudioFormat sourceFormat = audioStream.getFormat();
        float currentSampleRate = sourceFormat.getSampleRate();
        if(currentSampleRate<TARGET_SAMPLE_RATE-SAMPLE_RATE_OFFSET || currentSampleRate>TARGET_SAMPLE_RATE+SAMPLE_RATE_OFFSET){
            System.out.println("-Targeting 48KHz-");
            //AudioFormat resampledAudioFormat = new AudioFormat(TARGET_SAMPLE_RATE, sourceFormat.getSampleSizeInBits(), sourceFormat.getChannels(), true, true);
            boolean isSupported = AudioSystem.isConversionSupported(TARGET_FORMAT, sourceFormat);
            if(!isSupported){
                System.err.println("-Unable to convert-");
                return new AudioConversionResult(audioStream);
            }
            System.out.println("Frame length before conversion to 48Khz: " + audioStream.getFrameLength());
            audioStream = AudioSystem.getAudioInputStream(TARGET_FORMAT, audioStream);
            AudioFormat convertedFormat = audioStream.getFormat();
            long newFrameLengthOfAudioStream = audioStream.getFrameLength();
            if(newFrameLengthOfAudioStream <= 1){
                System.out.println("Frame length: " + newFrameLengthOfAudioStream + ", stream was not released? make sure you have tritonus library installed");
            }
            int size = (int) (newFrameLengthOfAudioStream* convertedFormat.getFrameSize());
            System.out.println("New audioStream frame_length*frame_size = " + size);
            //lengthSeconds = audioStream.getFrameLength() / convertedFormat.getFrameRate();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(size);
            int bytesWritten = 0;
            try{
                bytesWritten = AudioSystem.write(audioStream, AudioFileFormat.Type.AIFF, bytes);
            }catch (IOException ioException){
                System.err.println("Caught an error at writing");
            }
            System.out.println("Bytes written: " + bytesWritten);
            byte[] songBytes = bytes.toByteArray();
            return new AudioConversionResult(songBytes, audioStream);
        }
        return new AudioConversionResult(audioStream);
    }

    //removes header metadata from byte array
    public static byte[] removeMetadata(byte[] songBytes){
        byte[] rawAudio = new byte[songBytes.length - WAVE_OFFSET];
        System.arraycopy(songBytes, WAVE_OFFSET, rawAudio, 0, rawAudio.length);
        return rawAudio;
    }
}
