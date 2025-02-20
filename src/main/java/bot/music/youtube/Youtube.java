package bot.music.youtube;

import no4j.core.Logger;
import youtube_lib.Config;
import youtube_lib.YoutubeDownloader;
import youtube_lib.downloader.request.RequestVideoFileDownload;
import youtube_lib.downloader.request.RequestVideoInfo;
import youtube_lib.downloader.request.RequestVideoStreamDownload;
import youtube_lib.downloader.response.Response;
import youtube_lib.model.videos.VideoInfo;
import youtube_lib.model.videos.formats.Format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Youtube{
    private static final Logger logger = Logger.getLogger("primary");

    private final YoutubeDownloader downloader;
    private final Config config;

    public Youtube(){
        this.downloader = new YoutubeDownloader();
        this.config = downloader.getConfig();
    }

    public Config getConfig(){
        return config;
    }
    public YoutubeDownloader getDownloader(){
        return downloader;
    }

    //accounts for short links, ampersands
    public static String getVideoId(String youtubeLink){
        int index = youtubeLink.indexOf("?v=");
        if(index == -1){
            index = youtubeLink.indexOf("be/");
        }
        if(index == -1){
            index = youtubeLink.indexOf("ts/");
        }
        if(index != -1 && youtubeLink.length() > index+3){
            //make sure it's not joined with an ampersand, whitespace or a question mark
            int endIndex = -1;
            loop:
            for (int i = index + 3, len = youtubeLink.length(); i < len; i++){
                switch (youtubeLink.charAt(i)){
                    case ' ':
                    case '&':
                    case '?':
                    case '/':
                        endIndex = i;
                        break loop;
                }
            }
            //likely ends with an - out of bounds character
            if(endIndex == -1){
                return youtubeLink.substring(index + 3);
            }
            //ends with any of the specified four chars
            return youtubeLink.substring(index + 3, endIndex);
        }
        return "";
    }

    public YoutubeVideoInfo getVideoInformation(String videoId){
        Response<VideoInfo> response = downloader.getVideoInfo(new RequestVideoInfo(videoId));
        if(!response.ok()){
            return null;
        }
        VideoInfo videoInfo = null;
        try{
            videoInfo = response.data(3, TimeUnit.SECONDS);
        }catch (TimeoutException e){
            logger.stackTrace("Timed out", e);
        }
        return new YoutubeVideoInfo(videoInfo);
    }
    public File downloadFile(Format anyFormat, int timeoutSeconds){
        Response<File> response = downloader.downloadVideoFile(new RequestVideoFileDownload(anyFormat));
        File audioFile = null;
        try{
            audioFile = response.data(timeoutSeconds, TimeUnit.SECONDS);
        }catch (TimeoutException e){
            logger.stackTrace("Timed out", e);
        }
        return audioFile;
    }

    //TODO
    protected RequestVideoStreamDownload getStream(StreamType type){
        Format format = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RequestVideoStreamDownload streamDownload = new RequestVideoStreamDownload(null, baos);
        //return new RequestVideoStreamDownload(null,null);
        return null;
    }
}
