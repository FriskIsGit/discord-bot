package bot.deskort.commands.lyrics;

import bot.deskort.Bot;
import bot.deskort.commands.Command;
import bot.deskort.commands.Commands;
import bot.utilities.requests.SimpleResponse;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.http.client.fluent.Request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

public class LyricsCommand extends Command{
    private static final int MIN_LEN_THRESHOLD = 3;
    private static final String GENIUS_URL = "https://genius.com";
    private static final String GENIUS_API_URL = "https://api.genius.com";

    private final String ACCESS_TOKEN;
    public LyricsCommand(String... aliases){
        super(aliases);
        description = "Display song info and lyrics.\n" +
                      "-all displays all initially retrieved songs";
        usage = "lyrics `title`\n" +
                "lyrics `title` -a\n" +
                "lyrics `title` -all\n" +
                "lyrics `url`\n";
        ACCESS_TOKEN = Bot.getConfig().geniusToken;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(message == null)
            return;

        MessageChannelUnion channel = message.getChannel();
        if(args.length == 0){
            actions.messageChannel(channel, "No argument provided.");
            return;
        }

        String url = args[0];
        if(url.startsWith(GENIUS_URL)){
            Request webpageRequest = Request.Get(url);
            webpageRequest.addHeader("Accept", "text/html");
            webpageRequest.addHeader("Accept-Language", "en-US;q=0.7");
            webpageRequest.userAgent("Mozilla/5.0 Gecko/20100101");
            SimpleResponse pageResponse = SimpleResponse.performRequest(webpageRequest).expect("No response");
            if (pageResponse.code != 200){
                //exception
                return;
            }
            String lyrics = Scrapper.scrapeGeniusLyrics(pageResponse.body);
            actions.sendAsMessageBlock(channel, lyrics);
            return;
        }

        int len = args.length;
        boolean all = len > 1 && (args[len-1].equals("-all") || args[len-1].equals("-a"));
        String song = all ? Commands.mergeTerms(args, 0, args.length-1) : Commands.mergeTerms(args);

        Request request = Request.Get(GENIUS_API_URL + "/search?q=" + encode(song))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + ACCESS_TOKEN);

        SimpleResponse response = SimpleResponse.performRequest(request).expect("Simple response is null");
        JSONObject jsonBody = JSONObject.parseObject(response.body);
        JSONArray hits = jsonBody.getJSONObject("response").getJSONArray("hits");
        int songs = hits.size();
        if(songs == 0){
            actions.messageChannel(channel, "No songs found");
            return;
        }

        SongInfo[] songInfoArr = new SongInfo[songs];
        for (int i = 0; i < songs; i++){
            JSONObject hit = hits.getJSONObject(i).getJSONObject("result");
            songInfoArr[i] = SongInfo.fromJson(hit);
        }

        if(all){
            for (int i = 0; i < songs; i++){
                actions.sendEmbed(channel, embedSong(songInfoArr[i]));
            }
            return;
        }
        SongInfo mostAccurate = selectMostAccurate(song, songInfoArr);
        if(mostAccurate == null)
            return;

        actions.sendEmbed(channel, embedSong(mostAccurate));
        if(mostAccurate.lyricsURL == null)
            return;

        //scrape lyrics request
        if (!mostAccurate.lyricsURL.startsWith("https://genius.com")){
            return;
        }

        Request webpageRequest = Request.Get(mostAccurate.lyricsURL);
        webpageRequest.addHeader("Accept", "text/html");
        webpageRequest.addHeader("Accept-Language", "en-US;q=0.7");
        webpageRequest.userAgent("Mozilla/5.0 Gecko/20100101");
        SimpleResponse pageResponse = SimpleResponse.performRequest(webpageRequest).expect("No response");
        if (pageResponse.code != 200){
            //exception
            return;
        }
        String lyrics = Scrapper.scrapeGeniusLyrics(pageResponse.body);
        actions.sendAsMessageBlock(channel, lyrics);
    }

    //name and target will be split into targets
    public static int accuracy(String name, String target){
        if(name.length() == 0 || target.length() == 0){
            return 0;
        }
        return accuracy(name.split(" "), target.split(" "));
    }
    public static int accuracy(String[] names, String[] targets){
        for (int i = 0; i < names.length; i++) names[i] = names[i].toLowerCase();
        for (int i = 0; i < targets.length; i++) targets[i] = targets[i].toLowerCase();

        int accuracy = 0;
        for (String q : names){
            for (String t : targets){
                int tmp = matchingLen(q, t);
                if (tmp >= MIN_LEN_THRESHOLD){
                    accuracy += tmp;
                }
                if(tmp >= q.length()){
                    break;
                }
            }
        }
        int[] lengths = {0, 0};
        Arrays.stream(names).forEach(s -> lengths[0] += s.length());
        Arrays.asList(targets).forEach(s -> lengths[1] += s.length());
        accuracy = Math.min(accuracy, lengths[0]);
        return Math.min(accuracy, lengths[1]);
    }

    private static SongInfo selectMostAccurate(String name, SongInfo[] songs){
        int len = songs.length;
        if(len == 0){
            return null;
        }
        double[] accuracies = new double[len];
        double max = 0;
        int index = -1;
        for (int i = 0; i < len; i++){
            String title = songs[i].fullTitle;
            accuracies[i] = accuracy(name, title);
            accuracies[i] /= title.length();
            if(accuracies[i] > max){
                max = accuracies[i];
                index = i;
            }
        }
        System.out.println(Arrays.toString(accuracies));
        if(index == -1){
            index = 0;
        }
        return songs[index];
    }

    public static int matchingLen(String str1, String str2){
        int score = 0;
        int minLen = Math.min(str1.length(), str2.length());
        for (int i = 0, j = 0; i < minLen && j < minLen; i++, j++){
            char chr1 = str1.charAt(i);
            char chr2 = str2.charAt(j);
            if(chr1 == chr2){
                //coat -> cot
                //coat -> cost
                score++;
            }else{
                if(j+1 < str2.length() && chr1 == str2.charAt(j+1)){
                    // cot -> coat (missed letter)
                    score++;
                    j++;
                }
                else if(i+1 < str1.length() && str1.charAt(i+1) == chr2){
                    // coat -> cot (additional letter)
                    score++;
                    i++;
                }
            }
        }
        return score;
    }

    private static String encode(String toEncode){
        try{
            return URLEncoder.encode(toEncode, "utf-8");
        }catch (UnsupportedEncodingException ignored){
        }
        throw new IllegalStateException("Unreachable code");
    }

    public static MessageEmbed embedSong(SongInfo song){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setThumbnail(song.thumbnailURL);
        String title = song.fullTitle;
        if(title.length() > MessageEmbed.TITLE_MAX_LENGTH){
            title = title.substring(0, MessageEmbed.TITLE_MAX_LENGTH);
        }
        embed.setTitle(title);
        embed.setDescription(song.release);
        embed.appendDescription("\n" + song.lyricsURL);
        return embed.build();
    }
}


class SongInfo{
    public int id;
    public String fullTitle, lyricsURL, thumbnailURL, release;

    //parses "result" key
    public static SongInfo fromJson(JSONObject json){
        SongInfo info = new SongInfo();
        //artist_names is not an array
        info.fullTitle = json.getString("artist_names") + " - " + json.getString("title");
        info.thumbnailURL = json.getString("header_image_thumbnail_url");
        info.lyricsURL = json.getString("url");
        info.id = json.getInteger("id");
        info.release = json.getString("release_date_for_display");
        return info;
    }

    @Override
    public String toString(){
        return "SongInfo{" +
                "id=" + id +
                ", fullTitle='" + fullTitle + '\'' +
                ", lyricsURL='" + lyricsURL + '\'' +
                ", thumbnailURL='" + thumbnailURL + '\'' +
                ", release=" + release +
                '}';
    }
}