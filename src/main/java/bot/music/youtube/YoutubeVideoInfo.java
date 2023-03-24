package bot.music.youtube;

import youtube_lib.downloader.model.videos.VideoDetails;
import youtube_lib.downloader.model.videos.VideoInfo;
import youtube_lib.downloader.model.videos.formats.AudioFormat;
import youtube_lib.downloader.model.videos.formats.Format;
import youtube_lib.downloader.model.videos.formats.VideoFormat;
import youtube_lib.downloader.model.videos.formats.VideoWithAudioFormat;

import java.util.List;
import java.util.stream.Collectors;

public class YoutubeVideoInfo{

    private AudioFormat discordAudioFormat;
    private AudioFormat bestM4AFormat;
    private VideoInfo videoInfo;
    private VideoDetails videoDetails;
    private static final char NEW_LINE = '\n';

    public YoutubeVideoInfo(VideoInfo videoInfo){
        if(videoInfo == null){
            return;
        }
        this.videoInfo = videoInfo;
        this.videoDetails = videoInfo.details();
    }

    public String detailsToString(){
        if(videoDetails == null){
            return "Invalid video, no details available";
        }
        return "Title: " + videoDetails.title() + NEW_LINE +
                "Views: " + videoDetails.viewCount() + NEW_LINE +
                "Length (seconds): " + videoDetails.lengthSeconds() + NEW_LINE +
                "Author: " + videoDetails.author() + NEW_LINE +
                "Downloadable: " + videoDetails.isDownloadable() + NEW_LINE +
                "Live: " + videoDetails.isLive() + NEW_LINE;
    }
    public String bestFormatsToString(){
        if(videoInfo == null){
            return "Invalid video, no formats available";
        }
        StringBuilder formatsStr = new StringBuilder();

        VideoFormat bestVidFormat = bestVideoFormat();
        AudioFormat bestAudioFormat = bestAudioFormat();
        VideoFormat bestVideoWithAudioFormat = bestVideoWithAudioFormat();

        if(bestVidFormat != null){
            formatsStr.append("Best VideoFormat:");
            formatsStr.append(videoFormatToString(bestVidFormat, -1));
        }

        if(bestAudioFormat != null){
            formatsStr.append("Best AudioFormat:");
            formatsStr.append(audioFormatToString(bestAudioFormat, -1));
        }

        if(bestVideoWithAudioFormat != null){
            formatsStr.append("Best VideoWithAudioFormat:");
            formatsStr.append(videoFormatToString(bestVideoWithAudioFormat, -1));
        }

        return formatsStr.toString();
    }

    //returns null if wasn't found
    public AudioFormat getDiscordAudioFormat(){
        if(discordAudioFormat != null){
            return discordAudioFormat;
        }
        //targets 48Khz and highest available bitrate
        int bestBitrate = 0;
        List<AudioFormat> audioFormats = audioFormats();
        for (AudioFormat af : audioFormats){
            if(af.audioSampleRate() == 48000){
                if(bestBitrate < af.bitrate()){
                    bestBitrate = af.bitrate();
                    discordAudioFormat = af;
                }
            }
        }
        return discordAudioFormat;
    }
    public AudioFormat getBestM4AFormat(){
        if(bestM4AFormat != null){
            return bestM4AFormat;
        }
        //targets highest available sample rate
        int highestSampleRate = 0;
        List<AudioFormat> audioFormats = audioFormats();
        for (AudioFormat af : audioFormats){
            if(af.extension().toString().equals("m4a")){
                if(highestSampleRate < af.audioSampleRate()){
                    highestSampleRate = af.audioSampleRate();
                    bestM4AFormat = af;
                }
            }
        }
        return bestM4AFormat;
    }
    public String getAvailableVideoWithAudioFormats(){
        StringBuilder formatsAsStr = new StringBuilder();
        List<VideoWithAudioFormat> formats = videoWithAudioFormats();
        int size = formats.size();
        formatsAsStr.append("Available video with audio formats(").append(size).append(')').append(NEW_LINE);

        int formatIndex = 0;
        for (VideoWithAudioFormat format : formats){
            String formatAsString = videoWithAudioFormatToString(format,formatIndex++);
            formatsAsStr.append(formatAsString);
        }
        return formatsAsStr.toString();
    }

    public String getAvailableAudioFormats(){
        StringBuilder audioFormatsAsStr = new StringBuilder();
        List<AudioFormat> audioFormats = audioFormats();
        int size = audioFormats.size();
        audioFormatsAsStr.append("Available audio formats (").append(size).append(')').append(NEW_LINE);

        int formatIndex = 0;
        for (AudioFormat af : audioFormats){
            String formatAsString = audioFormatToString(af, formatIndex++);
            audioFormatsAsStr.append(formatAsString);
        }
        return audioFormatsAsStr.toString();
    }

    public String getAvailableVideoFormats(){
        StringBuilder videoFormatsAsStr = new StringBuilder();
        List<VideoFormat> videoFormats = videoFormats();
        int size = videoFormats.size();
        videoFormatsAsStr.append("Available video formats (").append(size).append(")").append(NEW_LINE);

        int formatIndex = 0;
        for (VideoFormat vf : videoFormats){
            String formatAsString = videoFormatToString(vf,formatIndex++);
            videoFormatsAsStr.append(formatAsString);
        }
        return videoFormatsAsStr.toString();
    }

    public static String audioFormatToString(AudioFormat audioFormat, int index){
        StringBuilder propertiesStr = new StringBuilder(100);
        if(index > -1){
            propertiesStr.append(index);
        }
        propertiesStr
                .append("|sample rate ").append(audioFormat.audioSampleRate())
                .append(" |audio quality ").append(audioFormat.audioQuality())
                .append(" |bitrate ").append(audioFormat.bitrate())
                .append(" |extension ").append(audioFormat.extension().value());
        String sizeInMBs = bytesToMBs(audioFormat.contentLength());
        propertiesStr.append(" | size ").append(sizeInMBs).append(NEW_LINE);
        return propertiesStr.toString();
    }

    private static String bytesToMBs(long contentLength){
        double MBs = contentLength/(1024*1024D);
        return String.format("%.2f", MBs);
    }

    public static String videoFormatToString(VideoFormat videoFormat, int index){
        StringBuilder propertiesStr = new StringBuilder(128);
        if(index > -1){
            propertiesStr.append(index);
        }
        propertiesStr.append("| ")
                .append(videoFormat.width()).append('x').append(videoFormat.height())
                .append(" |fps ").append(videoFormat.fps())
                .append(" |quality ").append(videoFormat.videoQuality())
                .append(" |bitrate ").append(videoFormat.bitrate())
                .append(" |ext ").append(videoFormat.extension().value());
        String sizeInMBs;
        String sizeKind = " | size ";
        if(videoFormat.contentLength() == null){
            sizeKind = " | est size ";
            sizeInMBs = bitrateToSizeAsStr(videoFormat.duration(),videoFormat.bitrate());
        }else{
            sizeInMBs = bytesToMBs(videoFormat.contentLength());
        }
        propertiesStr.append(sizeKind).append(sizeInMBs).append(NEW_LINE);
        return propertiesStr.toString();
    }
    public static String videoWithAudioFormatToString(VideoWithAudioFormat videoAudioFormat, int index){
        StringBuilder propertiesStr = new StringBuilder(128);
        if(index > -1){
            propertiesStr.append(index);
        }
        propertiesStr.append("| ")
                .append(videoAudioFormat.width()).append('x').append(videoAudioFormat.height())
                .append(" |fps ").append(videoAudioFormat.fps())
                .append(" |quality ").append(videoAudioFormat.videoQuality())
                .append(" |bitrate ").append(videoAudioFormat.bitrate())
                .append(" |audio quality ").append(videoAudioFormat.audioQuality())
                .append(" |ext ").append(videoAudioFormat.extension().value());
        String sizeInMBs;
        String sizeKind = " | size ";
        if(videoAudioFormat.contentLength() == null){
            sizeKind = " | est size ";
            sizeInMBs = bitrateToSizeAsStr(videoAudioFormat.duration(),videoAudioFormat.bitrate());
        }else{
            sizeInMBs = bytesToMBs(videoAudioFormat.contentLength());
        }

        propertiesStr.append(sizeKind).append(sizeInMBs).append(NEW_LINE);
        return propertiesStr.toString();
    }

    public static String bitrateToSizeAsStr(long durationInMillis, int bitrate){
        int bytesPerSecond = bitrate /8;
        long allBytes = durationInMillis / 1000 * bytesPerSecond;
        double MBs = ((double)allBytes / 1024D / 1024D);
        return String.format("%.2f", MBs);
    }
    public static double bitrateToSizeAsDouble(long durationInMillis, int bitrate){
        int bytesPerSecond = bitrate /8;
        long allBytes = durationInMillis / 1000 * bytesPerSecond;
        return ((double)allBytes / 1024D / 1024D);
    }

    public List<AudioFormat> audioFormats(){
        return videoInfo.audioFormats();
    }
    public List<VideoWithAudioFormat> videoWithAudioFormats(){
        return videoInfo.videoWithAudioFormats();
    }
    public List<VideoFormat> videoFormats(){
        List<VideoFormat> videoFormats = videoInfo.videoFormats();
        return videoFormats.stream()
                .filter(anyFormat -> anyFormat.type().equals("video"))
                .collect(Collectors.toList());
    }
    public List<Format> formats(){
        return videoInfo.formats();
    }
    public VideoFormat bestVideoFormat(){
        return videoInfo.bestVideoFormat();
    }
    public AudioFormat bestAudioFormat(){
        return videoInfo.bestAudioFormat();
    }
    public VideoFormat bestVideoWithAudioFormat(){
        return videoInfo.bestVideoWithAudioFormat();
    }
}
