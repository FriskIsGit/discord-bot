package bot.music.youtube;

import youtube_lib.downloader.YoutubeDownloader;
import youtube_lib.downloader.downloader.request.RequestVideoFileDownload;
import youtube_lib.downloader.downloader.response.Response;
import youtube_lib.downloader.model.Extension;
import youtube_lib.downloader.model.videos.formats.AudioFormat;
import youtube_lib.downloader.model.videos.formats.Format;
import youtube_lib.downloader.model.videos.formats.VideoWithAudioFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRequest{
    public final static int DOWNLOAD_SIZE_LIMIT_MB = 100;

    private final Youtube youtube;
    private final YoutubeDownloader youtubeDownloader;

    public UserRequest(Youtube youtube){
        this.youtube = youtube;
        this.youtubeDownloader = youtube.getDownloader();
    }

    public UserResponse executeRequest(YoutubeRequest request){
        if(request.type == StreamType.NONE){
            return UserResponse.fail("Invalid request");
        }
        YoutubeVideoInfo ytVideoInfo = youtube.getVideoInformation(request.videoId);
        if(ytVideoInfo == null){
            return UserResponse.fail("Unable to get info about video (check the URL)");
        }

        if(request.type == StreamType.INFO){
            return infoResponse(ytVideoInfo);
        }

        return downloadImpl(request.type, ytVideoInfo, request.formatNumber);
    }

    private UserResponse infoResponse(YoutubeVideoInfo ytVideoInfo){
        String msg = ytVideoInfo.detailsToString() + '\n' +
                ytVideoInfo.getAvailableVideoFormats() + '\n' +
                ytVideoInfo.getAvailableVideoFormats() + '\n' +
                ytVideoInfo.getAvailableVideoWithAudioFormats() + '\n' +
                "Alleged best formats: \n" + ytVideoInfo.bestFormatsToString();
        return UserResponse.success(msg);
    }

    private UserResponse downloadImpl(StreamType type, YoutubeVideoInfo info, int formatNumber){
        List<Format> formats;
        switch (type){
            case VIDEO:
                formats = new ArrayList<>(info.videoFormats());
                break;
            case AUDIO:
                formats = new ArrayList<>(info.audioFormats());
                break;
            case VIDEO_AUDIO:
                formats = new ArrayList<>(info.videoWithAudioFormats());
                break;
            default:
                return UserResponse.fail("Unreachable code reached");
        }

        if (formatNumber >= formats.size()){
            return UserResponse.fail(type + " format out of range");
        }
        Format desiredFormat = formats.get(formatNumber);
        double formatSize = getFormatSize(desiredFormat);
        if(formatSize > DOWNLOAD_SIZE_LIMIT_MB){
            //TODO investigate
            return UserResponse.fail("Format size is larger than " + DOWNLOAD_SIZE_LIMIT_MB + " MBs");
        }

        UUID uuid = UUID.randomUUID();
        RequestVideoFileDownload request = new RequestVideoFileDownload(desiredFormat).renameTo(String.valueOf(uuid));

        long st = System.currentTimeMillis();
        Response<File> downloadResponse = youtubeDownloader.downloadVideoFile(request);
        File video = downloadResponse.data();
        long en = System.currentTimeMillis();

        if(video == null){
            return UserResponse.fail("Request returned no data");
        }else if(!downloadResponse.ok()){
            return UserResponse.fail("Response not OK");
        }

        long millisTaken  = en - st;
        System.out.println("Absolute path for new file: " + video.getAbsolutePath());
        System.out.println("Time taken on download: " + millisTaken);

        return UserResponse.success("File downloaded (" + millisTaken + ')', video);
    }

    private double getFormatSize(Format format){
        Long duration = format.duration();
        Integer bitrate = format.bitrate();
        return YoutubeVideoInfo.bitrateToSizeAsDouble(duration, bitrate);
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
