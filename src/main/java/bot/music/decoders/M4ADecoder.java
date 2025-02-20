package bot.music.decoders;

import net.sourceforge.jaad.aac.AACException;
import no4j.core.Logger;
import org.jcodec.common.*;
import org.jcodec.common.model.AudioBuffer;
import org.jcodec.common.model.Packet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

// Fails to decode audio file if chunk sample tables are missing (all .m4a (audio only) yt videos)
public class M4ADecoder{
    private static final Logger logger = Logger.getLogger("primary");
    private static final int NULL_THRESHOLD = 22;
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
            if (fileFormat == null) {
                logger.warn("File format is null");
                return;
            }
            demuxer = JCodecUtil.createDemuxer(fileFormat, m4aFile);
            audioTrack = demuxer.getAudioTracks().get(0);

        }catch (IOException e){
            logger.stackTrace("", e);
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

        logger.debug("Duration: " + audioTrack.getMeta().getTotalDuration());
        logger.debug("Audio codec: " + audioTrack.getMeta().getCodec());
        logger.debug("Total frames: " + audioTrack.getMeta().getTotalFrames());

        Packet encodedPacket;
        boolean firstPacket = true;
        int nulls = 0;
        int frames = 0;
        AudioDecoder audioDecoder = JCodecUtil.createAudioDecoder(Codec.AAC, audioTrack.getMeta().getCodecPrivate());
        if (audioDecoder == null){
            logger.warn("Audio decoder null, quitting?");
            return 0;
        }
        do{
            encodedPacket = audioTrack.nextFrame();
            if (encodedPacket == null){
                if (++nulls > NULL_THRESHOLD){
                    logger.debug("Packet was null " + NULL_THRESHOLD + "+ times in a row.. quitting");
                    break;
                }
                logger.debug("Packet null..");
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
        }catch (IOException e){
            logger.stackTrace("Couldn't finalize demuxer", e);
        }
    }
}
