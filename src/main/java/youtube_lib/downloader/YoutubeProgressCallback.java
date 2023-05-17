package youtube_lib.downloader;

public interface YoutubeProgressCallback<T> extends YoutubeCallback<T> {

    void onDownloading(int progress);

}
