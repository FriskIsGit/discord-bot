package bot.music.youtube;

import youtube_lib.downloader.Config;
import youtube_lib.downloader.YoutubeDownloader;
import youtube_lib.downloader.downloader.request.RequestVideoFileDownload;
import youtube_lib.downloader.downloader.request.RequestVideoInfo;
import youtube_lib.downloader.downloader.request.RequestVideoStreamDownload;
import youtube_lib.downloader.downloader.response.Response;
import youtube_lib.downloader.model.videos.VideoInfo;
import youtube_lib.downloader.model.videos.formats.AudioFormat;
import youtube_lib.downloader.model.videos.formats.Format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class Youtube{
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
        if(index != -1 && youtubeLink.length() > index+3){
            //make sure it's not joined with an ampersand or ends in whitespace
            int ampersand = youtubeLink.indexOf('&',index + 3);
            int whitespace = youtubeLink.indexOf(' ', index + 3);
            //neither do they end in whitespace nor contain an ampersand
            if(ampersand == whitespace){
                return youtubeLink.substring(index + 3);
            }
            if(ampersand == -1){
                //has to contain whitespace
                return youtubeLink.substring(index + 3, whitespace);
            }
            //has to contain ampersand
            return youtubeLink.substring(index + 3, ampersand);
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
        }catch (TimeoutException timeoutExc){
            timeoutExc.printStackTrace();
        }
        return new YoutubeVideoInfo(videoInfo);
    }
    public File downloadFile(AudioFormat audioFormat){
        Response<File> response = downloader.downloadVideoFile(new RequestVideoFileDownload(audioFormat));
        File audioFile = null;
        try{
            audioFile = response.data(20, TimeUnit.SECONDS);
        }catch (TimeoutException timeoutExc){
            timeoutExc.printStackTrace();
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
