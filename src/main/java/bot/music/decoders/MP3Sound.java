package bot.music.decoders;

import fr.delthas.javamp3.Sound;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MP3Sound{
    protected Path path;
    private Sound sound;
    public MP3Sound(String filePath) throws IOException{
        path = Paths.get(filePath);
        if(!Files.exists(path)){
            throw new IOException("File doesn't exist");
        }
    }
    private void createSound(){
        if(sound != null){
            return;
        }
        try{
            InputStream inputStream = Files.newInputStream(path);
            sound = new Sound(new BufferedInputStream(inputStream));
        }catch (IOException ioException){
            ioException.printStackTrace();
        }catch (ArrayIndexOutOfBoundsException aioobExc){
            aioobExc.printStackTrace();
            System.err.println("Decoder messed up");
        }
    }

    public Sound getSound(){
        createSound();
        return sound;
    }
}
