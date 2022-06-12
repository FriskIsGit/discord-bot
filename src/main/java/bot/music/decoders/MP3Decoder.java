package bot.music.decoders;

import javazoom.jl.decoder.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MP3Decoder{

    protected Decoder decoder;
    protected Bitstream mp3Bitstream;
    protected Path path;
    protected ByteArrayOutputStream baos;
    protected byte[] rawAudio;

    public MP3Decoder(String filePath) throws IOException{
        path = Paths.get(filePath);
        if(!Files.exists(path)){
            throw new IOException("File doesn't exist");
        }
        decoder = new Decoder();
        mp3Bitstream = new Bitstream(new FileInputStream(filePath));
        baos = new ByteArrayOutputStream();
    }
    public int decodeEntireMP3(){
        return decodeEntireMP3(Integer.MAX_VALUE);
    }
    public int decodeEntireMP3(int frames){
        int totalRead = 0;
        try{
            while(frames-- > 0){
                Header headerFrame = mp3Bitstream.readFrame();
                if(headerFrame == null) {
                    break;
                }
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(headerFrame, mp3Bitstream);
                short [] buf = output.getBuffer();

                totalRead+= buf.length;

                baos.write(convertToByteArray(buf));

                mp3Bitstream.closeFrame();
            }

        }catch (BitstreamException | DecoderException | IOException decodingExc){
            decodingExc.printStackTrace();
            System.err.println("Failed decoding");
        }

        return totalRead;
    }
    private static byte[] convertToByteArray(short[] shortArr){
        int n = shortArr.length;
        byte[] result = new byte[2*n];
        for(int i = 0; i<2*n -1; i+=2){
            result[i] = (byte) (shortArr[i>>1] & 0xff);
            result[i+1] = (byte) ((shortArr[i>>1 | 1] >> 8) & 0xff);
        }
        return result;
    }

    public ByteArrayOutputStream getByteArrayOutputStream(){
        return baos;
    }
    public byte[] getStreamBytes(){
        if(rawAudio == null){
            rawAudio = baos.toByteArray();
        }
        return rawAudio;
    }

    public ByteArrayInputStream getByteArrInStream(){
        if(rawAudio == null){
            rawAudio = baos.toByteArray();
        }
        return new ByteArrayInputStream(rawAudio);
    }
}
