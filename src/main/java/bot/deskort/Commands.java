package bot.deskort;

import java.util.HashMap;

public abstract class Commands{
    final static HashMap<String, RequestFunction> COMMANDS_TO_FUNCTIONS = new HashMap<String, RequestFunction>() {{

        put("logs",        new RequestFunction(MessageProcessor::logsRequest,       true));
        put("shutdown",    new RequestFunction(MessageProcessor::shutdownRequest,   true));

        put("join",        new RequestFunction(MessageProcessor::joinRequest,       false));
        put("warp",        new RequestFunction(MessageProcessor::warpRequest,       false));
        put("play",        new RequestFunction(MessageProcessor::playRequest,       false));
        put("stop",        new RequestFunction(MessageProcessor::stopRequest,       false));
        put("leave",       new RequestFunction(MessageProcessor::leaveRequest,      false));
        put("loop",        new RequestFunction(MessageProcessor::loopRequest,       false));
        put("regain",      new RequestFunction(MessageProcessor::regain,            false));
        put("yt",          new RequestFunction(MessageProcessor::youtubeRequest,    false));
        RequestFunction    tracksRequestFunction = new RequestFunction(MessageProcessor::tracksRequest, false);
        put("tracks",      tracksRequestFunction);
        put("songs",       tracksRequestFunction);

        put("purge",       new RequestFunction(MessageProcessor::purgeRequest,      false));
        put("uptime",      new RequestFunction(MessageProcessor::uptimeRequest,     true));
        put("help",        new RequestFunction(MessageProcessor::helpRequest,       false));
        put("sig",         new RequestFunction(MessageProcessor::signatureCheck,    false));
        put("gentoken",    new RequestFunction(MessageProcessor::genTokenRequest,   true));

        RequestFunction    memoryRequestFunction = new RequestFunction(MessageProcessor::memoryRequest, false);
        put("memstat",     memoryRequestFunction);
        put("mempanel",    memoryRequestFunction);
        put("clrsongs",    new RequestFunction(MessageProcessor::clearSongsRequest, true));
        put("gc",          new RequestFunction(MessageProcessor::GCRequest,         true));

        RequestFunction    hashRequestFunction = new RequestFunction(MessageProcessor::stringHashRequest, false);
        put("hash",        hashRequestFunction);
        put("sha256",      hashRequestFunction);
        put("md5",         hashRequestFunction);
        put("sha",         hashRequestFunction);

    }};

}
