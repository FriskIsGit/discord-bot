package bot.music.youtube;

import youtube_lib.downloader.model.videos.VideoDetails;
import youtube_lib.downloader.model.videos.VideoInfo;
import youtube_lib.downloader.model.videos.formats.AudioFormat;
import youtube_lib.downloader.model.videos.formats.Format;
import youtube_lib.downloader.model.videos.formats.VideoFormat;
import youtube_lib.downloader.model.videos.formats.VideoWithAudioFormat;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class YoutubeVideoInfo{
    private ByteArrayOutputStream baos;
    private AudioFormat discordAudioFormat;
    private VideoInfo videoInfo;
    private VideoDetails videoDetails;
    private static final char NEW_LINE = '\n';

    public YoutubeVideoInfo(VideoInfo videoInfo){

        if(videoInfo == null){
            return;
        }
        this.videoInfo = videoInfo;
        this.videoDetails = videoInfo.details();

        this.baos = new ByteArrayOutputStream();
    }

    public String detailsToString(){
        if(videoDetails == null){
            return "Invalid video, no details available";
        }
        StringBuilder detailsStr = new StringBuilder();
        detailsStr.append("Title ")          .append(videoDetails.title())         .append(NEW_LINE)
                .append("Views: ")           .append(videoDetails.viewCount())     .append(NEW_LINE)
                .append("Length (seconds): ").append(videoDetails.lengthSeconds()) .append(NEW_LINE)
                .append("Author: ")          .append(videoDetails.author())        .append(NEW_LINE)
                .append("Downloadable: ")    .append(videoDetails.isDownloadable()).append(NEW_LINE)
                .append("Live: ")            .append(videoDetails.isLive())        .append(NEW_LINE);
        return detailsStr.toString();
    }
    public String bestFormats(){
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
    public String getAvailableVideoWithAudioFormats(){
        StringBuilder formatsAsStr = new StringBuilder();
        List<VideoWithAudioFormat> formats = videoWithAudioFormats();
        int size = formats.size();
        formatsAsStr.append("Available video with audio formats(").append(size).append(")").append(NEW_LINE);

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
        audioFormatsAsStr.append("Available audio formats (").append(size).append(")").append(NEW_LINE);

        int formatIndex = 0;
        for (AudioFormat af : audioFormats){
            String formatAsString = audioFormatToString(af,formatIndex++);
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
        StringBuilder propertiesStr = new StringBuilder();
        propertiesStr.append(index);
        propertiesStr.append(" |type ").append(audioFormat.type())
                     .append(" |sample rate ").append(audioFormat.audioSampleRate())
                     .append(" |bitrate ").append(audioFormat.bitrate())
                     .append(" |avg bitrate ").append(audioFormat.averageBitrate())
                     .append(" |extension ").append(audioFormat.extension().value());
        String sizeInMBs = bitrateToSizeAsStr(audioFormat.duration(), audioFormat.bitrate());
        propertiesStr.append(" |est size ").append(sizeInMBs).append(NEW_LINE);
        return propertiesStr.toString();
    }
    public static String videoFormatToString(VideoFormat videoFormat, int index){
        StringBuilder propertiesStr = new StringBuilder();
        propertiesStr.append(index);
        propertiesStr.append(" |type ").append(videoFormat.type())
                     .append(" |fps ").append(videoFormat.fps())
                     .append(" |width ").append(videoFormat.width())
                     .append(" |height ").append(videoFormat.height())
                     .append(" |bitrate ").append(videoFormat.bitrate())
                     .append(" |extension ").append(videoFormat.extension().value());
        String sizeInMBs = bitrateToSizeAsStr(videoFormat.duration(), videoFormat.bitrate());
        propertiesStr.append(" |est size ").append(sizeInMBs).append(NEW_LINE);
        return propertiesStr.toString();
    }
    public static String videoWithAudioFormatToString(VideoWithAudioFormat videoAudioFormat, int index){
        StringBuilder propertiesStr = new StringBuilder();
        propertiesStr.append(index);
        propertiesStr.append(" |type ").append(videoAudioFormat.type())
                     .append(" |fps ").append(videoAudioFormat.fps())
                     .append(" |width ").append(videoAudioFormat.width())
                     .append(" |height ").append(videoAudioFormat.height())
                     .append(" |bitrate ").append(videoAudioFormat.bitrate())
                     .append(" |avg bitrate ").append(videoAudioFormat.averageBitrate())
                     .append(" |audio quality ").append(videoAudioFormat.audioQuality())
                     .append(" |extension ").append(videoAudioFormat.extension().value());
        String sizeInMBs = bitrateToSizeAsStr(videoAudioFormat.duration(), videoAudioFormat.bitrate());
        propertiesStr.append(" |est size ").append(sizeInMBs).append(NEW_LINE);
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

    public List<AudioFormat> audioFormats(){return videoInfo.audioFormats();}
    public List<VideoWithAudioFormat> videoWithAudioFormats(){return videoInfo.videoWithAudioFormats();}
    public List<VideoFormat> videoFormats(){
        return videoInfo.videoFormats();
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
