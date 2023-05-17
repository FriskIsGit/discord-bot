package youtube_lib.downloader;

import java.io.File;

import youtube_lib.downloader.request.RequestWebpage;
import youtube_lib.downloader.request.RequestVideoFileDownload;
import youtube_lib.downloader.request.RequestVideoStreamDownload;
import youtube_lib.downloader.response.Response;

public interface Downloader {

    Response<String> downloadWebpage(RequestWebpage request);

    Response<File> downloadVideoAsFile(RequestVideoFileDownload request);

    Response<Void> downloadVideoAsStream(RequestVideoStreamDownload request);

}
