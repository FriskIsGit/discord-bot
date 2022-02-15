package bot.music.youtube;

import youtube_lib.downloader.YoutubeDownloader;
import youtube_lib.downloader.downloader.request.RequestVideoFileDownload;
import youtube_lib.downloader.downloader.response.Response;
import youtube_lib.downloader.model.Extension;
import youtube_lib.downloader.model.videos.formats.AudioFormat;
import youtube_lib.downloader.model.videos.formats.Format;
import youtube_lib.downloader.model.videos.formats.VideoFormat;
import youtube_lib.downloader.model.videos.formats.VideoWithAudioFormat;
import bot.deskort.Bot;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import bot.utilities.Actions;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class YoutubeRequest{
    volatile private static boolean hasActiveRequest;
    volatile private static long lastRequest = 0;
    public static int DOWNLOAD_SIZE_LIMIT_MB = 100;
    public static int REQUEST_RATE_LIMIT_MS = 4000;
    public static int MIN_REQUEST_LENGTH = 6;
    protected Youtube youtube;
    protected YoutubeVideoInfo ytVideoInfo;
    protected YoutubeDownloader youtubeDownloader;
    protected String requestText;
    protected MessageChannel requestChannel;
    protected long requestChannelId;
    protected Actions actions;

    public YoutubeRequest(Youtube youtube, MessageReceivedEvent messageReceivedEvent){

        hasActiveRequest = false;
        this.youtube = youtube;
        this.youtubeDownloader = youtube.getDownloader();

        this.requestText = messageReceivedEvent.getMessage().getContentRaw();
        this.requestChannel = messageReceivedEvent.getMessage().getChannel();
        this.requestChannelId = requestChannel.getIdLong();

        this.actions = Bot.getActions();
    }

    public static boolean hasActiveRequest(){
        return hasActiveRequest;
    }
    public static void flipRequestState(){
        hasActiveRequest^= true;
    }
    //>ytinfo
    //>ytvi
    //>ytau
    public void process(){
        if(requestText.length() < MIN_REQUEST_LENGTH){
            actions.sendAsMessageBlock(requestChannel.getIdLong(), "Invalid request");
            return;
        }

        if(System.currentTimeMillis()-lastRequest < REQUEST_RATE_LIMIT_MS){
            actions.sendAsMessageBlock(requestChannel.getIdLong(), "Yer bweein rate limited uwu nya ;3 0.0");
            return;
        }
        lastRequest = System.currentTimeMillis();

        YoutubeRequestParser parser = new YoutubeRequestParser(requestText);
        ParsedResult result = parser.parse();
        YoutubeVideoInfo ytVideoInfo = youtube.getVideoInformation(result.videoId);
        if(ytVideoInfo == null){
            actions.messageChannel(requestChannelId,"Unable to get info about video");
            return;
        }
        this.ytVideoInfo = ytVideoInfo;

        if(result.type == StreamType.INFO){
            displayVideoInformation();
        }else if(result.type == StreamType.AUDIO){
            downloadAudio(result.formatNumber);
        }
        else if(result.type == StreamType.VIDEO){
            downloadVideo(result.formatNumber);
        }
        else if(result.type == StreamType.VIDEO_AUDIO){
            downloadVideoWithAudio(result.formatNumber);
        }

    }

    private void displayVideoInformation(){
        actions.sendAsMessageBlock(requestChannelId, ytVideoInfo.detailsToString());
        actions.sendAsMessageBlock(requestChannelId, ytVideoInfo.getAvailableVideoFormats());
        actions.sendAsMessageBlock(requestChannelId, ytVideoInfo.getAvailableAudioFormats());
        actions.sendAsMessageBlock(requestChannelId, ytVideoInfo.getAvailableVideoWithAudioFormats());
        actions.sendAsMessageBlock(requestChannelId, "Alleged best formats: \n" + ytVideoInfo.bestFormats());
    }

    private void downloadVideo(int formatNumber){
        List<VideoFormat> videoFormatList = ytVideoInfo.videoFormats();
        if (formatNumber >= videoFormatList.size()){
            actions.messageChannel(requestChannelId, "Format out of range");
            return;
        }
        VideoFormat desiredVidFormat = videoFormatList.get(formatNumber);
        double formatSize = getFormatSize(desiredVidFormat);
        if(formatSize > DOWNLOAD_SIZE_LIMIT_MB){
            actions.messageChannel(requestChannelId, formatExceededMessage(formatSize));
            return;
        }

        UUID uuid = UUID.randomUUID();
        RequestVideoFileDownload request = new RequestVideoFileDownload(desiredVidFormat).renameTo(String.valueOf(uuid));

        actions.sendAsMessageBlock(requestChannelId, "Downloading: \n" + YoutubeVideoInfo.videoFormatToString(desiredVidFormat,-1));

        flipRequestState();
        long st = System.currentTimeMillis();
        Response<File> downloadResponse = youtubeDownloader.downloadVideoFile(request);
        File video = downloadResponse.data();
        long en = System.currentTimeMillis();
        flipRequestState();

        if(video == null){
            actions.messageChannel(requestChannelId, "Request returned no data");
            return;
        }else if(!downloadResponse.ok()){
            actions.messageChannel(requestChannelId, "Response not okii");
        }

        System.out.println("Absolute path for new file: " + video.getAbsolutePath());
        System.out.println("Time taken on download: " + (en-st));

        actions.sendFile(requestChannelId, video);
    }


    private void downloadVideoWithAudio(int formatNumber){
        List<VideoWithAudioFormat> availableFormats = ytVideoInfo.videoWithAudioFormats();
        if (formatNumber >= availableFormats.size()){
            actions.messageChannel(requestChannelId, "Format out of range");
            return;
        }

        VideoWithAudioFormat desiredFormat = availableFormats.get(formatNumber);
        double formatSize = getFormatSize(desiredFormat);
        if(formatSize > DOWNLOAD_SIZE_LIMIT_MB){
            actions.messageChannel(requestChannelId, formatExceededMessage(formatSize));
            return;
        }

        actions.sendAsMessageBlock(requestChannelId, "Downloading: \n" + YoutubeVideoInfo.videoWithAudioFormatToString(desiredFormat,-1));
        UUID uuid = UUID.randomUUID();
        RequestVideoFileDownload request = new RequestVideoFileDownload(desiredFormat).renameTo(String.valueOf(uuid));

        flipRequestState();
        long st = System.currentTimeMillis();
        Response<File> response = youtubeDownloader.downloadVideoFile(request);
        File videoWithAudio = response.data();
        long en = System.currentTimeMillis();
        flipRequestState();
        if(videoWithAudio == null){
            actions.messageChannel(requestChannelId, "Request returned no data");
            return;
        }
        System.out.println("Absolute path for new file: " + videoWithAudio.getAbsolutePath());
        System.out.println("Time taken on download: " + (en-st));

        actions.sendFile(requestChannelId, videoWithAudio);
    }

    private void downloadAudioImpl(AudioFormat format){
        RequestVideoFileDownload request = new RequestVideoFileDownload(format).renameTo(String.valueOf(UUID.randomUUID()));

        flipRequestState();
        long st = System.currentTimeMillis();
        Response<File> response = youtubeDownloader.downloadVideoFile(request);
        File audioFile = response.data();
        long en = System.currentTimeMillis();
        flipRequestState();

        if(audioFile == null){
            actions.messageChannel(requestChannelId, "Request returned no data");
            return;
        }
        System.out.println("Absolute path for new file: " + audioFile.getAbsolutePath());
        System.out.println("Time taken on download: " + (en-st));
        actions.sendFile(requestChannelId,audioFile);
    }

    private void downloadAudio(int formatNumber){
        AudioFormat requestedFormat;
        List<AudioFormat> formatList = ytVideoInfo.audioFormats();
        if (formatNumber >= formatList.size()){
            actions.messageChannel(requestChannelId, "Format out of range");
            return;
        }

        requestedFormat = formatList.get(formatNumber);
        double formatSize = getFormatSize(requestedFormat);
        if(formatSize > DOWNLOAD_SIZE_LIMIT_MB){
            actions.messageChannel(requestChannelId, formatExceededMessage(formatSize));
            return;
        }
        downloadAudioImpl(requestedFormat);
    }

    private double getFormatSize(Format format){
        return YoutubeVideoInfo.bitrateToSizeAsDouble(format.duration(),format.bitrate());
    }

    private static String formatExceededMessage(double formatSize){
        return "Format size of "  + String.format("%.2f", formatSize) + " MBs exceeds " + DOWNLOAD_SIZE_LIMIT_MB;
    }

    private VideoWithAudioFormat findWorstBitrate(List<VideoWithAudioFormat> availableFormats){
        VideoWithAudioFormat lowestQualityFormat = null;
        int worstBitrate = Integer.MAX_VALUE;
        for(VideoWithAudioFormat format : availableFormats){
            if(format.extension() == Extension.MPEG4){
                if(worstBitrate > format.bitrate()){
                    worstBitrate = format.bitrate();
                    lowestQualityFormat = format;
                }
            }
        }
        return lowestQualityFormat;
    }
    private AudioFormat findBestBitrate(List<AudioFormat> availableFormats){
        AudioFormat bestFormat = null;
        int bestBitrate = 0;
        for(AudioFormat format : availableFormats){
            if(format.extension() == Extension.M4A){
                if(format.bitrate() > bestBitrate){
                    bestBitrate = format.bitrate();
                    System.out.println("M4A available");
                    bestFormat = format;
                }
            }
        }
        return bestFormat;
    }

}
