package bot.textprocessors;

import bot.core.Bot;
import bot.utilities.FileSeeker;
import bot.utilities.Hasher;
import bot.utilities.jda.Actions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * balls.dat file should end with a new line
 * All information is stored in balls.dat in the following formats:
 * ---------------------
 * hash:name:identifier
 * hash:name
 * ---------------------
 * hash - sha256 hash of the .png file
 * name - any valid name of the ball
 * identifier - (optional) C - country, whether ball should be considered a country
 */
public class BallsDex extends TextProcessor{
    public final HashMap<String, CountryBall> sha256ToBall = new HashMap<>();
    public final Hint hints = Hint.NON_COUNTRIES_ONLY;

    private boolean readDatFile = false;
    private Path ballsPath = null;
    private Actions actions;
    private Message message;
    private MessageChannelUnion channel;
    private String lastHash;

    public BallsDex(){
        System.out.println(mapToData());
        if(readDatFile){
            return;
        }
        FileSeeker fs = new FileSeeker("balls.dat");
        String ballsPath = fs.findTargetPath();
        if(ballsPath.isEmpty()){
            System.err.println("Country balls data file not found");
            return;
        }
        this.ballsPath = Paths.get(ballsPath);
        readBallsDat();
    }

    private void readBallsDat(){
        List<String> lines;
        try{
            lines = Files.readAllLines(ballsPath);
        }catch (IOException e){
            e.printStackTrace();
            return;
        }
        for(String line : lines){
            String[] components = line.split(":");
            switch (components.length){
                case 0:
                case 1:
                    continue;
                case 2:
                    sha256ToBall.put(components[0], new CountryBall(components[1], false));
                    break;
                default:
                    boolean isCountry = !components[2].isEmpty() && components[2].charAt(0) == 'C';
                    sha256ToBall.put(components[0], new CountryBall(components[1], isCountry));
                    break;
            }
        }
        readDatFile = true;
    }

    public void appendBallToFile(String hash, CountryBall ball){
        StringBuilder definition = new StringBuilder();
        try{
            definition.append(hash).append(':').append(ball.name);
            if(ball.isCountry){
                definition.append(":C");
            }
            definition.append('\n');
            Files.write(ballsPath, definition.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }catch (IOException e){
            System.err.println(e.getMessage());
        }
    }

    public String mapToData(){
        StringBuilder str = new StringBuilder();
        for(Map.Entry<String, CountryBall> entry : sha256ToBall.entrySet()){
            CountryBall ball = entry.getValue();
            str.append(entry.getKey()).append(':').append(ball.name);
            if(ball.isCountry){
                str.append(":C");
            }
            str.append('\n');
        }
        return str.toString();
    }

    @Override
    boolean consume(String content, Message message, boolean isEdit){
        long authorId = message.getAuthor().getIdLong();
        if(authorId != 999736048596816014L && authorId != 1073275888466145370L)
            return false;

        this.message = message;
        this.channel = message.getChannel();
        actions = Bot.getActions();
        List<Message.Attachment> attachments = message.getAttachments();
        if(content.startsWith("A wild country") && attachments.size() == 1 && !isEdit){
            Message.Attachment image = attachments.get(0);
            byte[] imageBytes = retrieveCountry(image);
            resolveHash(imageBytes);
        }else if(content.contains("You caught") && isEdit){
            if(lastHash == null || sha256ToBall.containsKey(lastHash)){
                return false;
            }
            String ballName = extractName(content);
            System.out.println("Discovered:[" + lastHash + ':' + ballName + ']');
            CountryBall ball = new CountryBall(ballName, false);
            sha256ToBall.put(lastHash, ball);
            appendBallToFile(lastHash, ball);
        }
        return false;
    }

    private static String extractName(String content){
        int index = content.indexOf("You caught");
        if(index == -1){
            return "";
        }
        int exclamation = content.indexOf('!', index);
        return content.substring(index+13, exclamation);
    }

    private void resolveHash(byte[] bytes){
        String hash = Hasher.hashBytes(bytes, Hasher.choose("sha256"));
        lastHash = hash;
        if(sha256ToBall.containsKey(hash)){
            CountryBall country = sha256ToBall.get(hash);
            displayAccordingToHints(country);
        }else{
            actions.messageChannel(channel, hash);
        }
    }

    private byte[] retrieveCountry(Message.Attachment image){
        File tempDir = new File("tmp");
        tempDir.mkdir();
        File temp = new File("tmp/temp_file");
        temp.delete();
        try{
            temp = image.getProxy().downloadToFile(temp).get(8, TimeUnit.SECONDS);
        }catch (InterruptedException | ExecutionException | TimeoutException e){
            actions.messageChannel(channel, "Image download failed");
            System.err.println(e.getMessage());
            return null;
        }
        try{
            return Files.readAllBytes(temp.toPath());
        }catch (IOException e){
            System.err.println(e.getMessage());
            return null;
        }finally{
            temp.delete();
            tempDir.deleteOnExit();
        }
    }

    private void displayAccordingToHints(CountryBall country){
        if(hints == Hint.ALL){
            actions.messageChannel(message.getChannel(), country.name);
        }else if(hints == Hint.COUNTRIES_ONLY && country.isCountry){
            actions.messageChannel(message.getChannel(), country.name);
        }else if(hints == Hint.NON_COUNTRIES_ONLY && !country.isCountry){
            actions.messageChannel(message.getChannel(), "Non-country: " + country.name);
        }
    }

}

enum Hint{
    ALL, COUNTRIES_ONLY, NON_COUNTRIES_ONLY
}

class CountryBall{
    public final String name;
    public final boolean isCountry;

    public CountryBall(String name, boolean isCountry){
        this.name = name;
        this.isCountry = isCountry;
    }

    @Override
    public String toString(){
        return "[" + name + ':' + isCountry + "]";
    }
}
