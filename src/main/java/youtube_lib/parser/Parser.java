package youtube_lib.parser;

import youtube_lib.downloader.request.RequestChannelUploads;
import youtube_lib.downloader.request.RequestPlaylistInfo;
import youtube_lib.downloader.request.RequestSearchable;
import youtube_lib.downloader.request.RequestVideoInfo;
import youtube_lib.downloader.response.Response;
import youtube_lib.model.playlist.PlaylistInfo;
import youtube_lib.model.videos.VideoInfo;
import youtube_lib.model.subtitles.SubtitlesInfo;
import youtube_lib.model.search.SearchResult;
import youtube_lib.downloader.request.RequestSubtitlesInfo;
import youtube_lib.downloader.request.RequestSearchResult;
import youtube_lib.downloader.request.RequestSearchContinuation;

import java.util.List;
public interface Parser {

    /* Video */
    Response<VideoInfo> parseVideo(RequestVideoInfo request);

    /* Playlist */
    Response<PlaylistInfo> parsePlaylist(RequestPlaylistInfo request);

    /* Channel uploads */
    Response<PlaylistInfo> parseChannelsUploads(RequestChannelUploads request);

    /* Subtitles */
    Response<List<SubtitlesInfo>> parseSubtitlesInfo(RequestSubtitlesInfo request);

    /* Search */
    Response<SearchResult> parseSearchResult(RequestSearchResult request);

    Response<SearchResult> parseSearchContinuation(RequestSearchContinuation request);

    Response<SearchResult> parseSearcheable(RequestSearchable request);

}
