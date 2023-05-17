package youtube_lib.downloader;

public interface YoutubeCallback<T> {
    void onFinished(T data);
    void onDownloading(int progress);
    void onError(Throwable throwable);
}
