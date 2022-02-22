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
        Response<File> response = this.getDownloader().downloadVideoFile(new RequestVideoFileDownload(audioFormat));
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
