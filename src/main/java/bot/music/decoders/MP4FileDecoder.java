package bot.music.decoders;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Track;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

//very limited use, JAAD is unreliable to decode most containers
public class MP4FileDecoder{
    private Decoder dec;
    private net.sourceforge.jaad.mp4.api.AudioTrack track;
    private AudioFormat originalFormat;
    public MP4FileDecoder(File file){
        InputStream is;
        try{
            is = Files.newInputStream(file.toPath());
            MP4Container mp4Container = new MP4Container(is);
            //find AAC track
            final List<Track> tracks = mp4Container.getMovie().getTracks(AudioTrack.AudioCodec.AAC);
            if(tracks.isEmpty()){
                throw new IllegalStateException("Does not contain any AAC track");
            }
            track = (AudioTrack) tracks.get(0);
            dec = new Decoder(track.getDecoderSpecificInfo());
            originalFormat = new AudioFormat(track.getSampleRate(), track.getSampleSize(), track.getChannelCount(), true, true);

        }catch (IOException ioException){
            System.err.println("Encountered error initializing m4a decoder");
            ioException.printStackTrace();
        }
    }
    public MP4FileDecoder(String filePath){
        this(new File(filePath));
    }
    public byte[] decodeFully() throws IOException{
        byte[] b;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);

        final SampleBuffer sampleBuffer = new SampleBuffer();
        while(track.hasMoreFrames()) {
            Frame frame = track.readNextFrame();
            try {
                dec.decodeFrame(frame.getData(), sampleBuffer);
                b = sampleBuffer.getData();
                baos.write(b,0,b.length);
            }
            catch(AACException aacExc) {
                aacExc.printStackTrace();
                //since the frames are separate, decoding can continue if one fails
            }
        }
        return baos.toByteArray();
    }
    public AudioFormat getOriginalFormat(){
        return originalFormat;
    }
}
