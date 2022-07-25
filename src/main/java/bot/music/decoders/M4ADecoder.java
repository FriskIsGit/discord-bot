package bot.music.decoders;

import net.sourceforge.jaad.aac.AACException;
import org.jcodec.common.*;
import org.jcodec.common.model.AudioBuffer;
import org.jcodec.common.model.Packet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

//will not decode if file lacks chunk sample tables (all .m4a (audio only) yt videos)
public class M4ADecoder{
    private final static int NULL_THRESHOLD = 22;
    private AudioFormat decodedAudioFormat;
    private DemuxerTrack audioTrack;
    private ByteArrayOutputStream streamBytes;
    private Demuxer demuxer;

    public M4ADecoder(File m4aFile) throws FileNotFoundException{
        if(!m4aFile.exists()){
            throw new FileNotFoundException(m4aFile.getAbsolutePath() + " path doesn't exist");
        }
        try{
            Format fileFormat = JCodecUtil.detectFormat(m4aFile);
            if (fileFormat == null){
                System.out.println("File format is null");
                return;
            }
            demuxer = JCodecUtil.createDemuxer(fileFormat, m4aFile);
            audioTrack = demuxer.getAudioTracks().get(0);

        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
    public M4ADecoder(String path) throws FileNotFoundException{
        this(new File(path));
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
                if (++nulls > NULL_THRESHOLD){
                    System.out.println("Packet was null " + NULL_THRESHOLD + "+ times in a row.. quitting");
                    break;
                }
                System.out.println("Packet null..");
                continue;
            }
            frames++;
            AudioBuffer decodedAudioBuffer;
            try{
                decodedAudioBuffer = audioDecoder.decodeFrame(encodedPacket.data, null);
            }catch (AACException aacExc){
                frames--;
                continue;
            }


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
    @Override
    protected void finalize(){
        try{
            demuxer.close();
        }catch (IOException ioExc){
            System.out.println("Couldn't finalize demuxer");
            ioExc.printStackTrace();
        }
    }
}
