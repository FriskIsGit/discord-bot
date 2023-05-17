package youtube_lib.downloader.request;

import youtube_lib.model.playlist.PlaylistInfo;

public class RequestChannelUploads extends Request<RequestPlaylistInfo, PlaylistInfo>  {

    private final String channelId;

    public RequestChannelUploads(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }

}
