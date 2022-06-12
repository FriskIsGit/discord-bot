package bot.music.decoders;

import org.jcodec.common.*;
import org.jcodec.common.model.AudioBuffer;
import org.jcodec.common.model.Packet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

//will not decode if file lacks chunk sample tables (all .m4a (audio only) yt videos)
public class M4ADecoder{
    private AudioFormat decodedAudioFormat;
    private DemuxerTrack audioTrack;
    private ByteArrayOutputStream streamBytes;


    public M4ADecoder(File m4aFile){
        try{
            Format fileFormat = JCodecUtil.detectFormat(m4aFile);
            if (fileFormat == null){
                System.out.println("File format is null");
                return;
            }
            Demuxer demuxer = JCodecUtil.createDemuxer(fileFormat, m4aFile);
            audioTrack = demuxer.getAudioTracks().get(0);

        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
    public M4ADecoder(String path){
        if(Files.notExists(Paths.get(path))){
            System.out.println("  file path");
        }
        File file = new File(path);
        try{
            Format fileFormat = JCodecUtil.detectFormat(file);
            if (fileFormat == null){
                System.out.println("File format is null");
                return;
            }
            Demuxer demuxer = JCodecUtil.createDemuxer(fileFormat, file);
            audioTrack = demuxer.getAudioTracks().get(0);
            demuxer.close();

        }catch (IOException ioException){
            ioException.printStackTrace();
        }

    }

    public AudioFormat getDecodedAudioFormat(){
        return decodedAudioFormat;
    }
    public ByteArrayOutputStream getStreamBytes(){
        return streamBytes;
    }

    /**
     * decodes the entire track to raw audio
     * @return number of decoded frames
     * @throws IOException
     */
    public int decode() throws IOException{

        streamBytes = new ByteArrayOutputStream(4096);

        System.out.println("Duration: " + audioTrack.getMeta().getTotalDuration());
        System.out.println("Audio codec: " + audioTrack.getMeta().getCodec());
        System.out.println("Total frames: " + audioTrack.getMeta().getTotalFrames());

        Packet encodedPacket;
        boolean firstPacket = true;
        int nulls = 0;
        int frames = 0;
        AudioDecoder audioDecoder = JCodecUtil.createAudioDecoder(Codec.AAC, audioTrack.getMeta().getCodecPrivate());
        if (audioDecoder == null){
            System.out.println("Audio decoder null, quitting?");
            return 0;
        }
        do{
            encodedPacket = audioTrack.nextFrame();
            if (encodedPacket == null){
                if (++nulls > 3){
                    System.out.println("Packet was null 3+ times in a row.. quitting");
                    break;
                }
                System.out.println("Packet null..");
                continue;
            }
            frames++;
            AudioBuffer decodedAudioBuffer = audioDecoder.decodeFrame(encodedPacket.data, null);

            //usually endian order differs from the original
            if (firstPacket){
                firstPacket = false;
                //reading just the header format
                decodedAudioFormat = decodedAudioBuffer.getFormat();
            }

            byte[] decodedAudio = decodedAudioBuffer.getData().array();

            streamBytes.write(decodedAudio);

        }while (true);

        return frames;

    }
}
