package youtube_lib.downloader;

public interface YoutubeCallback<T> {

    void onFinished(T data);

    void onError(Throwable throwable);
}
