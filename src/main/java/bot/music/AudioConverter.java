package bot.music;

import bot.music.decoders.M4ADecoder;
import bot.music.decoders.MP3Decoder;
import bot.music.decoders.MP3Sound;
import no4j.core.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioConverter{
    private static final Logger logger = Logger.getLogger("primary");

    //ffmpeg -i inputFile.m4a -codec copy converted.m4a
    public final static short WAVE_OFFSET = 44;
    public final static float SAMPLE_RATE_OFFSET = 500f;
    public final static float TARGET_SAMPLE_RATE = 48000f;
    public final static AudioFormat TARGET_FORMAT = new AudioFormat(TARGET_SAMPLE_RATE, 16, 2, true,true);

    private AudioConverter() {
    }

    public static AudioConversionResult convertM4AToRaw(String m4aFilePath){

        M4ADecoder m4ADecoder;
        try{
            m4ADecoder = new M4ADecoder(m4aFilePath);
        }catch (FileNotFoundException e){
            logger.stackTrace("File not found", e);
            return null;
        }
        int frames = 0;
        try{
            frames = m4ADecoder.decode();
        }catch (IOException e){
            logger.stackTrace("Failed to decode", e);
        }
        if(frames <= 1){
            logger.debug("Frames decoded <= 1");
            return null;
        }
        byte[] songBytes = m4ADecoder.getStreamBytes().toByteArray();
        ByteArrayInputStream bytesStream = new ByteArrayInputStream(songBytes);
        //divided by sampleRate per frame? should be 4
        org.jcodec.common.AudioFormat decodedAudioFormat = m4ADecoder.getDecodedAudioFormat();
        javax.sound.sampled.AudioFormat pureJavaFormat = jcodecFormatToPureJavaFormat(decodedAudioFormat);

        AudioInputStream ais = new AudioInputStream(bytesStream, pureJavaFormat, songBytes.length/decodedAudioFormat.getFrameSize());
        return new AudioConversionResult(songBytes, ais);
    }

    public static AudioConversionResult convertMP3FileToRaw(String mp3FilePath){
        //audioFileLength / (frameSize * frameRate)
        MP3Decoder mp3Decoder;
        MP3Sound mp3Sound;
        try{
            mp3Sound = new MP3Sound(mp3FilePath);
            mp3Decoder = new MP3Decoder(mp3FilePath);
        }catch (IOException e){
            logger.stackTrace("", e);
            return null;
        }
        int decodedSamples = mp3Decoder.decodeEntireMP3();
        logger.debug("Decoded samples: " + decodedSamples);
        logger.debug("ByteArrInputStream size: " + mp3Decoder.getByteArrayOutputStream().size());
        byte[] songBytes = mp3Decoder.getStreamBytes();
        AudioInputStream audioStream = new AudioInputStream(mp3Decoder.getByteArrInStream(), mp3Sound.getSound().getAudioFormat(), decodedSamples/2);
        return new AudioConversionResult(songBytes, audioStream);
    }

    //inputStream = new AudioInputStream(new ByteArrayInputStream(new byte[]{}), AudioTrack.TARGET_FORMAT, length);
    public static AudioConversionResult target48Hz(AudioInputStream audioStream){
        AudioFormat sourceFormat = audioStream.getFormat();
        float currentSampleRate = sourceFormat.getSampleRate();
        if(currentSampleRate<TARGET_SAMPLE_RATE-SAMPLE_RATE_OFFSET || currentSampleRate>TARGET_SAMPLE_RATE+SAMPLE_RATE_OFFSET){
            logger.debug("-Targeting 48KHz-");
            //AudioFormat resampledAudioFormat = new AudioFormat(TARGET_SAMPLE_RATE, sourceFormat.getSampleSizeInBits(), sourceFormat.getChannels(), true, true);
            boolean isSupported = AudioSystem.isConversionSupported(TARGET_FORMAT, sourceFormat);
            if(!isSupported){
                logger.error("-Unable to convert-");
                return new AudioConversionResult(audioStream);
            }
            logger.debug("Frame length before conversion to 48Khz: " + audioStream.getFrameLength());
            audioStream = AudioSystem.getAudioInputStream(TARGET_FORMAT, audioStream);
            AudioFormat convertedFormat = audioStream.getFormat();
            long newFrameLengthOfAudioStream = audioStream.getFrameLength();
            if(newFrameLengthOfAudioStream <= 1){
                logger.warn("Frame length = " + newFrameLengthOfAudioStream + ", stream was not released?" +
                        "make sure you have tritonus library installed, it is possible that resampler failed");
                return new AudioConversionResult(audioStream);
            }
            int size = (int) (newFrameLengthOfAudioStream* convertedFormat.getFrameSize());
            logger.debug("New audioStream frame_length*frame_size = " + size);
            //lengthSeconds = audioStream.getFrameLength() / convertedFormat.getFrameRate();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(size);
            int bytesWritten = 0;
            try{
                bytesWritten = AudioSystem.write(audioStream, AudioFileFormat.Type.AIFF, bytes);
            }catch (IOException ioException){
                logger.error("Caught an error at writing");
            }
            logger.debug("Bytes written: " + bytesWritten);
            byte[] songBytes = bytes.toByteArray();
            return new AudioConversionResult(songBytes, audioStream);
        }
        return new AudioConversionResult(audioStream);
    }

    //removes header metadata from byte array
    public static byte[] removeWAVMetadata(byte[] songBytes){
        byte[] rawAudio = new byte[songBytes.length - WAVE_OFFSET];
        System.arraycopy(songBytes, WAVE_OFFSET, rawAudio, 0, rawAudio.length);
        return rawAudio;
    }

    public static javax.sound.sampled.AudioFormat jcodecFormatToPureJavaFormat(org.jcodec.common.AudioFormat givenFormat){
        if(givenFormat == null){
            logger.error("Given jcodec format is null");
            return null;
        }
        return new javax.sound.sampled.AudioFormat(
                givenFormat.getSampleRate(),
                givenFormat.getSampleSizeInBits(),
                givenFormat.getChannels(),
                givenFormat.isSigned(),
                givenFormat.isBigEndian());
    }
}
