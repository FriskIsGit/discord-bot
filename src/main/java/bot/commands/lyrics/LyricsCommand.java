package bot.commands.lyrics;

import bot.core.Bot;
import bot.commands.Command;
import bot.commands.Commands;
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
    private static final boolean FAVOR_ANY_VIEWS = true;
    private static final float FAVOR_FACTOR = 0.15f;

    private static final String GENIUS_URL = "https://genius.com";
    private static final String GENIUS_API_URL = "https://api.genius.com";

    public LyricsCommand(String... aliases){
        super(aliases);
        description = "Display song info and lyrics.\n" +
                      "-all displays all initially retrieved songs";
        usage = "lyrics `title`\n" +
                "lyrics `title` -a\n" +
                "lyrics `title` -all\n" +
                "lyrics `url`\n";
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
        song = deGeniusify(song);
        Request request = Request.Get(GENIUS_API_URL + "/search?q=" + encode(song))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + Bot.getConfig().geniusToken);

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
    public static float accuracy(String name, String target){
        if(name.length() == 0 || target.length() == 0){
            return 0;
        }
        return accuracy(name.split(" "), target.split(" "));
    }
    public static float accuracy(String[] names, String[] targets){
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
        int[] targetLen = new int[1];
        Arrays.stream(targets).forEach(s -> targetLen[0] += s.length());
        return Math.min((float)accuracy / targetLen[0], 1);
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
            SongInfo song = songs[i];
            accuracies[i] = accuracy(name, song.fullTitle);
            if(FAVOR_ANY_VIEWS && song.pageViews > 0){
                accuracies[i] += FAVOR_FACTOR;
            }
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
        for (int i = 0, j = 0; i < str1.length() && j < str2.length(); i++, j++){
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
    private static String deGeniusify(String songName){
        StringBuilder str = new StringBuilder();
        boolean inRoundBrackets = false, inSquareBrackets = false;
        int len = songName.length();
        for (int i = 0; i < len; i++){
            char chr = songName.charAt(i);
            switch (chr){
                case '-':
                    if (i == 0 || i == len-1){
                        continue;
                    }

                    if (songName.charAt(i-1) != ' '){
                        str.append(' ');
                    }
                    str.append('-');
                    if (i+1 < len && songName.charAt(i+1) != ' '){
                        str.append(' ');
                    }

                    break;
                case '[':
                    inSquareBrackets = true;
                    break;
                case '(':
                    inRoundBrackets = true;
                    break;
                case ')':
                    inRoundBrackets = false;
                    break;
                case ']':
                    inSquareBrackets = false;
                    break;
                default:
                    if (!inRoundBrackets && !inSquareBrackets)
                        str.append(chr);
                    break;
            }
        }
        return str.toString();
    }
}
