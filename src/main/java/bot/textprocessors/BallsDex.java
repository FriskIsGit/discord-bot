package bot.textprocessors;

import bot.core.Bot;
import bot.utilities.FileSeeker;
import bot.utilities.Hasher;
import bot.utilities.jda.Actions;
import net.dv8tion.jda.api.entities.Message;
import no4j.core.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
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
public class BallsDex extends TextProcessor {
    private static final Logger logger = Logger.getLogger("primary");

    public final HashMap<String, CountryBall> sha256ToBall = new HashMap<>();
    public final HashSet<String> countries = new HashSet<>();
    public final HashMap<Long, Dex> guildsToDex = new HashMap<>();
    public final Hint hints = Hint.NON_COUNTRIES_ONLY;
    public boolean displayHints = true;

    private static final long BALLS_DEX_ID = 999736048596816014L, WORLD_DEX_ID = 1073275888466145370L;
    private boolean readDatFile = false;
    private Path ballsPath = null;
    private Path countriesPath = null;
    private Actions actions;

    private Message message;
    private long authorId;
    private long guildId;

    public BallsDex() {
        logger.info(mapToData());
        if (readDatFile) {
            return;
        }
        FileSeeker seeker = new FileSeeker("balls.dat");
        String ballsPath = seeker.findTargetPath();
        if (ballsPath.isEmpty()) {
            logger.error("balls.dat not found");
            return;
        }
        this.ballsPath = Paths.get(ballsPath);
        readBallsDat();

        seeker = new FileSeeker("countries.dat");
        ballsPath = seeker.findTargetPath();
        if (ballsPath.isEmpty()) {
            logger.error("countries.dat not found");
            return;
        }
        this.countriesPath = Paths.get(ballsPath);
        readCountriesDat();
    }

    @Override
    boolean consume(String content, Message message, boolean isEdit) {

        return false;
    }

    private void readBallsDat() {
        List<String> lines;
        try {
            lines = Files.readAllLines(ballsPath);
        } catch (IOException e) {
            logger.stackTrace("Failed to read balls.dat", e);
            return;
        }
        for (String line : lines) {
            String[] components = line.split(":");
            switch (components.length) {
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

    private void readCountriesDat() {
        List<String> lines;
        try {
            lines = Files.readAllLines(countriesPath);
        } catch (IOException e) {
            logger.stackTrace("Failed to read countries.dat", e);
            return;
        }
        countries.addAll(lines);
    }

    public void appendBallToFile(String hash, CountryBall ball) {
        StringBuilder definition = new StringBuilder();
        try {
            definition.append(hash).append(':').append(ball.name);
            if (ball.isCountry) {
                definition.append(":C");
            }
            definition.append('\n');
            Files.write(ballsPath, definition.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.exception(e);
        }
    }

    public String mapToData() {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, CountryBall> entry : sha256ToBall.entrySet()) {
            CountryBall ball = entry.getValue();
            str.append(entry.getKey()).append(':').append(ball.name);
            if (ball.isCountry) {
                str.append(":C");
            }
            str.append('\n');
        }
        return str.toString();
    }

    private void addDiscoveredBall() {
        Dex dex = getDex();
        String lastHash;
        if (authorId == BALLS_DEX_ID) {
            lastHash = dex.lastBallsDexHash;
        } else {
            lastHash = dex.lastWorldDexHash;
        }
        if (lastHash == null || sha256ToBall.containsKey(lastHash)) {
            return;
        }

        String ballName = extractName(message.getContentRaw());
        logger.info("Discovered:[" + lastHash + ':' + ballName + ']');
        CountryBall ball = new CountryBall(ballName, countries.contains(ballName));
        sha256ToBall.put(lastHash, ball);
        appendBallToFile(lastHash, ball);
    }

    public void reloadBallsFromFile() {
        sha256ToBall.clear();
        readBallsDat();
    }

    private static String extractName(String content) {
        int index = content.indexOf("You caught");
        if (index == -1) {
            return "";
        }
        int exclamation = content.indexOf('!', index);
        return content.substring(index + 13, exclamation);
    }

    private void resolveHash(byte[] bytes) {
        if (bytes == null) {
            return;
        }
        String hash = Hasher.hashBytes(bytes, Hasher.choose("sha256"));
        Dex dex = getDex();

        if (authorId == BALLS_DEX_ID) {
            dex.lastBallsDexHash = hash;
        } else {
            dex.lastWorldDexHash = hash;
        }
        if (sha256ToBall.containsKey(hash)) {
            CountryBall country = sha256ToBall.get(hash);
            displayAccordingToHints(country);
        } else {
            actions.messageChannel(message.getChannel(), hash);
        }
    }

    private Dex getDex() {
        Dex dex;
        if (guildsToDex.containsKey(guildId)) {
            dex = guildsToDex.get(guildId);
        } else {
            dex = new Dex();
            guildsToDex.put(guildId, dex);
        }
        return dex;
    }

    private byte[] retrieveCountry(Message.Attachment image) {
        File tempDir = new File("tmp");
        tempDir.mkdir();
        File temp = new File("tmp/temp_file");
        temp.delete();
        try {
            temp = image.getProxy().downloadToFile(temp).get(8, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            actions.messageChannel(message.getChannel(), "Image download failed");
            logger.exception(e);
            return null;
        }
        try {
            return Files.readAllBytes(temp.toPath());
        } catch (IOException e) {
            logger.exception(e);
            return null;
        } finally {
            temp.delete();
            tempDir.deleteOnExit();
        }
    }

    private void displayAccordingToHints(CountryBall country) {
        if (!displayHints) {
            return;
        }
        if (hints == Hint.ALL) {
            actions.messageChannel(message.getChannel(), country.name);
        } else if (hints == Hint.COUNTRIES_ONLY && country.isCountry) {
            actions.messageChannel(message.getChannel(), country.name);
        } else if (hints == Hint.NON_COUNTRIES_ONLY && !country.isCountry) {
            actions.messageChannel(message.getChannel(), "Non-country: " + country.name);
        }
    }

}

enum Hint {
    ALL, COUNTRIES_ONLY, NON_COUNTRIES_ONLY
}

class CountryBall {
    public final String name;
    public final boolean isCountry;

    public CountryBall(String name, boolean isCountry) {
        this.name = name;
        this.isCountry = isCountry;
    }

    @Override
    public String toString() {
        return "[" + name + ':' + isCountry + "]";
    }
}

class Dex {
    public String lastBallsDexHash;
    public String lastWorldDexHash;

    @Override
    public String toString() {
        return "GuildDex{" +
                "lastBallsDexHash='" + lastBallsDexHash + '\'' +
                ", lastWorldDexHash='" + lastWorldDexHash + '\'' + '}';
    }
}