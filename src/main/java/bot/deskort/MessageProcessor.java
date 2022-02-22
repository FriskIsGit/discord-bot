package bot.deskort;

import bot.music.AudioTrack;
import bot.utilities.*;

import bot.music.AudioPlayer;
import bot.music.youtube.Youtube;
import bot.music.youtube.YoutubeRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.io.*;

import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MessageProcessor extends Commands{

    private static final int USER_ID_LENGTH = 18;
    private static final List<Button> interactiveButtons = new ArrayList<>(Arrays.asList(
            Button.primary("clrsongs",  "Free songs from memory"),
            Button.primary("gc",        "Run GC"),
            Button.primary("refresh",   "Refresh")
    ));
    private static final HashMap<Long, MessageDeque> channelIdsToMessageDeques = new HashMap<>();
    public static String PREFIX;
    public static int PREFIX_OFFSET;
    public static int PURGE_CAP = 1000;

    private static JDA jdaInterface;
    private static Actions actions;
    private static Channels channels;
    private static Youtube youtube;

    private static MessageReceivedEvent messageEvent;
    private static String messageText;
    private static long messageChannelId;

    private static String commandName;
    private static String commandArgs;

    static Runtime run = Runtime.getRuntime();
    protected final static String OS = System.getProperty("os.name");
    static int MAX_DEQUE_SIZE = 500;

    MessageProcessor(){
        actions = Bot.getActions();
        channels = Bot.getChannels();
        jdaInterface = Bot.getJDAInterface();
        youtube = new Youtube();
        PREFIX = Bot.PREFIX;
        PREFIX_OFFSET = Bot.PREFIX_OFFSET;
    }

    protected static void logsRequest(){
        if(messageText.toLowerCase(Locale.ENGLISH).startsWith("logs", PREFIX_OFFSET)){
            messageEvent.getMessage().delete().queue();
            MessageDeque deq = channelIdsToMessageDeques.get(messageChannelId);
            if(deq != null){
                deq.removeLast();
                deq.print();
            }
        }
    }

    private static void logMessage(){
        if(channelIdsToMessageDeques.containsKey(messageChannelId)){
            channelIdsToMessageDeques.get(messageChannelId).add(messageEvent);
        }else{
            MessageDeque deq = new MessageDeque(MAX_DEQUE_SIZE);
            deq.add(messageEvent);
            channelIdsToMessageDeques.put(messageChannelId,deq);
        }
    }

    protected static void processMessage(MessageReceivedEvent message, String messageContent, long idLong){

        messageEvent = message;
        messageText = messageContent;
        messageChannelId = idLong;
        logMessage();
        dispatchCommand();
    }

    private static void dispatchCommand(){
        boolean isBot = messageEvent.getAuthor().isBot();
        if(messageText.startsWith(PREFIX)){
            final String msgLowerCase = messageText.toLowerCase();
            final String[] splitCommand = Commands.doubleTermSplit(messageText);
            commandName = splitCommand[0].toLowerCase();
            commandArgs = splitCommand[1];
            if(commandName.isEmpty()){
                return;
            }

            RequestFunction funcToExecute;
            //System.out.println("=|" + commandName+ "|=");
            if(COMMANDS_TO_FUNCTIONS.containsKey(commandName)){
                funcToExecute = COMMANDS_TO_FUNCTIONS.get(commandName);
                if(!isBot || funcToExecute.isTriggerableByBot()){
                    funcToExecute.run();
                }
            }else{
                //youtube associated requests
                if(msgLowerCase.length() < PREFIX_OFFSET+2){
                    return;
                }
                if(msgLowerCase.charAt(PREFIX_OFFSET)=='y' && msgLowerCase.charAt(PREFIX_OFFSET+1)=='t'){
                    funcToExecute = COMMANDS_TO_FUNCTIONS.get("yt");
                    if(!isBot || funcToExecute.isTriggerableByBot()){
                        funcToExecute.run();
                    }
                }
                //hash aliases
                if(Hasher.hasAlgorithm(commandName)){
                    funcToExecute = COMMANDS_TO_FUNCTIONS.get("hash");
                    if(!isBot || funcToExecute.isTriggerableByBot()){
                        funcToExecute.run();
                    }
                }
            }
        }
        else if(messageText.startsWith("$") && !messageText.startsWith("$$")){
            try{
                linuxRequest();
            }catch (Exception exc){exc.printStackTrace();}
        }
    }

    protected static void shutdownRequest(){
        if(isAuthorAuthorized()){
            actions.sendAsMessageBlock(messageChannelId,"Shutting down");
            sleep(3000);
            System.exit(0);
        }
    }

    protected static void loopRequest(){
        AudioManager audioManager = messageEvent.getGuild().getAudioManager();
        AudioPlayer audioPlayer = (AudioPlayer) audioManager.getSendingHandler();
        if(audioPlayer ==  null){
            audioPlayer = new AudioPlayer(audioManager);
            audioManager.setSendingHandler(audioPlayer);
        }
        boolean isLooping = audioPlayer.switchLooping();
        actions.messageChannel(messageEvent.getChannel(),"**Looping set to " + isLooping + "**");
    }

    protected static void uptimeRequest(){
        int uptimeSeconds = (int) (Bot.getUptime()/1000);
        StringBuilder message = new StringBuilder("Uptime: ");
        int hr = uptimeSeconds/3600;
        int min = (uptimeSeconds/60)%60;
        int sec = uptimeSeconds%60;
        message.append(hr).append("h ")
                .append(min).append("m ")
                .append(sec).append('s');
        actions.sendAsMessageBlock(messageEvent.getTextChannel(), message.toString());
    }


    protected static void tracksRequest(){
        String[] fileNames = AudioPlayer.AUDIO_FILES_DIR.list();
        if(fileNames != null && fileNames.length > 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Available tracks");
            embedBuilder.setColor(Color.BLUE);
            StringBuilder stringBuilder = new StringBuilder();
            int tracks = 0;
            for (String file : fileNames){
                String name = FileSeeker.getNameWithoutExtension(file);
                String ext = FileSeeker.getExtension(file);
                if(name.isEmpty() || ext.isEmpty() || !AudioPlayer.isExtensionSupported(ext)){
                    continue;
                }
                tracks++;
                stringBuilder.append(name);
                stringBuilder.append('\n');
                if(tracks%26 == 0){
                    embedBuilder.addField(new MessageEmbed.Field("", stringBuilder.toString(),true));
                    stringBuilder = new StringBuilder();
                }
            }
            if(stringBuilder.length() != 0){
                embedBuilder.addField(new MessageEmbed.Field("", stringBuilder.toString(),true));
            }
            actions.sendEmbed((TextChannel) messageEvent.getChannel(), embedBuilder.build());
        }

    }

    protected static void stopRequest(){
        AudioManager audioManager = messageEvent.getGuild().getAudioManager();
        AudioPlayer audioPlayer = (AudioPlayer) audioManager.getSendingHandler();
        if(audioPlayer ==  null){
            audioPlayer = new AudioPlayer(audioManager);
            audioManager.setSendingHandler(audioPlayer);
            return;
        }
        audioPlayer.setPlaying(false);
    }


    protected static void regain(){

        Guild server = messageEvent.getGuild();
        AudioManager audioManager = server.getAudioManager();
        AudioChannel currentAudioChannel = audioManager.getConnectedChannel();
        if(currentAudioChannel == null){
            actions.messageChannel(messageChannelId,"Not connected");
            return;
        }
        List<VoiceChannel> audioChannels = server.getVoiceChannels();
        if(audioChannels.size() < 2){
            actions.messageChannel(messageChannelId,"Not enough channels");
            return;
        }
        VoiceChannel swapChannel = null;
        for(VoiceChannel vc : audioChannels){
            if(vc.getIdLong() != currentAudioChannel.getIdLong()){
                swapChannel = vc;
                break;
            }
        }
        Member botMember = server.getMember(jdaInterface.getSelfUser());
        if(botMember == null){
            return;
        }
        server.moveVoiceMember(botMember, swapChannel).complete();
        server.moveVoiceMember(botMember, currentAudioChannel).complete();

    }

    protected static void warpRequest(){
        Member msgAuthor = messageEvent.getMember();
        GuildVoiceState vcState = Objects.requireNonNull(msgAuthor).getVoiceState();
        if(vcState != null && !vcState.inAudioChannel()){
            return;
        }
        VoiceChannel destinationChannel = channels.getVoiceChannelIgnoreCase(commandArgs);
        if(destinationChannel == null){
            return;
        }
        messageEvent.getGuild().moveVoiceMember(msgAuthor, destinationChannel).queue();
    }

    protected static void leaveRequest(){
        AudioManager audioManager = messageEvent.getGuild().getAudioManager();
        audioManager.closeAudioConnection();
    }

    protected static void playRequest(){
        AudioManager audioManager = messageEvent.getGuild().getAudioManager();
        if(!audioManager.isConnected()){
            actions.messageChannel(messageEvent.getChannel(),"I'm not in channel");
            return;
        }
        AudioPlayer audioPlayer = addSendingHandlerIfNull(audioManager);
        if(messageText.length() > PREFIX_OFFSET + 4 + 1){
            if (!audioPlayer.setAudioTrack(commandArgs)){
                actions.messageChannel(messageChannelId,"Track doesn't exist");
                return;
            }
        }
        actions.sendEmbed(messageEvent.getTextChannel(), createPlayingEmbed(audioPlayer));
        audioPlayer.setPlaying(true);
    }

    private static MessageEmbed createPlayingEmbed(AudioPlayer audioPlayer){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Now playing");
        AudioTrack currentAudioTrack = audioPlayer.getCurrentAudioTrack();
        if(currentAudioTrack == null){
            return null;
        }
        embedBuilder.setDescription(currentAudioTrack.getTrackName());
        return embedBuilder.build();
    }
    protected static MessageEmbed createMemoryEmbed(){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Memory stats");
        embedBuilder.setColor(Color.BLACK);
        embedBuilder.addField(new MessageEmbed.Field("",   getUsedRuntimeMemoryAsString(),false));
        embedBuilder.addField(new MessageEmbed.Field("",   getSongsSizeInMemoryAsString(),false));
        return embedBuilder.build();
    }

    protected static void youtubeRequest(){
        AudioManager audioManager = messageEvent.getGuild().getAudioManager();
        addSendingHandlerIfNull(audioManager);
        /*if(YoutubeRequest.hasActiveRequest()){
               actions.messageChannel(messageChannelId,"Has an active request to process");
               return;
           }*/
        new Thread(new Runnable(){
            @Override
            public void run(){
                YoutubeRequest ytRequest = new YoutubeRequest(youtube, messageEvent);
                ytRequest.process();
            }
        }).start();
    }

    protected static void joinRequest(){
        AudioManager audioManager = messageEvent.getGuild().getAudioManager();
        addSendingHandlerIfNull(audioManager);
        //channel specific join
        final int commandLength = 4;
        if(messageText.length() > PREFIX_OFFSET+commandLength+1){
            int indexBlank = messageText.indexOf(' ');
            String parsedName = messageText.substring(indexBlank + 1);
            VoiceChannel voice = channels.getVoiceChannelIgnoreCase(parsedName);
            if(voice == null){
                actions.messageChannel(messageEvent.getChannel(),"VoiceChannel not found");
                return;
            }
            //if targeting another server
            if(messageEvent.getGuild().getIdLong() != voice.getGuild().getIdLong()){
                audioManager = voice.getGuild().getAudioManager();
                addSendingHandlerIfNull(audioManager);
            }
            audioManager.openAudioConnection(voice);
            return;
        }
        //channel
        Member member = messageEvent.getMember();
        if(member == null) return;
        GuildVoiceState membersVoiceState = member.getVoiceState();
        if(membersVoiceState != null){
            if(membersVoiceState.inAudioChannel()){
                //VoiceChannel is also fine
                AudioChannel voice = member.getVoiceState().getChannel();
                audioManager.openAudioConnection(voice);
            }else{
                actions.messageChannel((TextChannel) messageEvent.getChannel(),"Member not in voice");
            }
        }else{
            actions.messageChannel((TextChannel) messageEvent.getChannel(),"MemberVoiceState null");
        }
    }
    //returns new sendingHandler
    private static AudioPlayer addSendingHandlerIfNull(AudioManager audioManager){
        AudioPlayer sendingHandler;
        AudioPlayer currentHandler = (AudioPlayer) audioManager.getSendingHandler();
        if(currentHandler == null){
            sendingHandler = new AudioPlayer(audioManager);
            System.out.println("-Setting up sending handler-");
            audioManager.setSendingHandler(sendingHandler);
            return sendingHandler;
        }
        return currentHandler;
    }

    public static void memoryRequest(){
        messageEvent.getTextChannel()
                .sendMessageEmbeds(createMemoryEmbed())
                .setActionRow(interactiveButtons)
                .queue();
    }
    protected static String getUsedRuntimeMemoryAsString(){
        Runtime runtime = Runtime.getRuntime();
        long memUsed = runtime.totalMemory() - runtime.freeMemory();
        String valueFormatted = String.format("%.2f", memUsed/(1024*1024D));
        return "Total mem - free mem: ```" + valueFormatted + " MBs ```";
    }
    protected static String getSongsSizeInMemoryAsString(){
        String valueFormatted =  String.format("%.2f", AudioPlayer.getSongsSizeInMemory()/(1024*1024D));
        return "Songs size in memory: ```" + valueFormatted + " MBs ```";
    }

    protected static void helpRequest(){
        MessageChannel channel = messageEvent.getChannel();
        final String HELP_MESSAGE = "```" +
                " [Available commands]\n" +
                " req\n" +
                " purge <amount> - channel based purge (each channel has its own deque, incorporates retrieving history when needed)\n" +
                " warp <voiceChannelName> - warps you to a voice channel (provided you're in one already)\n" +
                " join <partialName> if left blank bot will attempt to join message author\n" +
                " play <track> - makes bot play its 48Khz 16bit stereo 2channel 4bytes/frame BIG.ENDIAN PCM Signed opus encoded audio\n" +
                " tracks - displays all available tracks, separated with '|', some may be distorted\n" +
                " loop - self explanatory\n" +
                " sha <text> - one of many hashing algorithms (e.g. md5, sha256)\n" +
                " mem - display memory management panel\n" +
                " uptime\n" +
                " [Youtube Commands] <format_number> index at which it appears counting from the top (0-indexed)\n" +
                " ytinfo <videoID/link> retrieves information about the youtube video, displaying available formats\n" +
                " ytaudio <videoID/link> <format_number> retrieves audio file in specified format\n" +
                " ytvideo <videoID/link> <format_number> retrieves video file in specified format\n" +
                " ytviau <videoID/link> <format_number> retrieves video with audio in specified format\n" +
                "```";
        channel.sendMessage(HELP_MESSAGE).queue();
    }

    protected static void purgeRequest(){

        final int expectedCommandLen = 5;
        if(messageText.length() < expectedCommandLen+PREFIX_OFFSET+1) return;
        if(!isAuthorAuthorized()) return;

        int parsingOffset = expectedCommandLen+PREFIX_OFFSET+1;

        String numToParse = messageText.substring(parsingOffset);
        int amount;
        try{
            amount = Integer.parseInt(numToParse);
        }catch (NumberFormatException nfExc){
            return;
        }
        if(amount > PURGE_CAP || amount<1){
            deleteRequestMessage();
            return;
        }
        //include purge request message
        amount++;
        MessageChannel channel = messageEvent.getChannel();
        MessageDeque cachedMessages = channelIdsToMessageDeques.get(messageChannelId);
        int deqAmount = Math.min(cachedMessages.size(), amount);

        String lastMessageId = popAndPurgeLastMessages(channel, deqAmount);
        amount = amount - deqAmount;

        boolean retrieved = false;
        long start = -1, end= -1;
        if(amount>0){
            retrieved = true;
            start = System.currentTimeMillis();

            int maxedHistories = amount/100;
            List<Message> historiesList = new ArrayList<>(maxedHistories + 1);
            for (int h = 0; h < maxedHistories; h++){
                MessageHistory.MessageRetrieveAction retrieveHistoryAction = MessageHistory.getHistoryBefore(channel,lastMessageId).limit(100);
                List<Message> aHistory = retrieveHistoryAction.complete().getRetrievedHistory();
                //almost always lastIndex == 99
                int lastIndex = aHistory.size()-1;
                lastMessageId = aHistory.get(lastIndex).getId();
                historiesList.addAll(aHistory);
            }
            //add leftovers
            MessageHistory.MessageRetrieveAction retrieveHistoryAction = MessageHistory.getHistoryBefore(channel,lastMessageId).limit(amount%100);
            List<Message> aHistory = retrieveHistoryAction.complete().getRetrievedHistory();
            historiesList.addAll(aHistory);

            List<CompletableFuture<Void>> completableFutureList = channel.purgeMessages(historiesList);
            completeInFuture(completableFutureList);

            end = System.currentTimeMillis();
        }

        if(retrieved) actions.messageChannel(channel,"Retrieve purge: " + (end-start));

    }

    private static void deleteRequestMessage(){
        Message msgToDelete = messageEvent.getMessage();
        msgToDelete.delete().queue();
        channelIdsToMessageDeques.get(messageChannelId).removeLast();
    }

    /**
     * @param channel - where the purge occurs
     * @param amount - excluding the purge request message
     * @returns oldest message id as a reference point
     **/
    private static String popAndPurgeLastMessages(MessageChannel channel, int amount){
        List<Message> list = channelIdsToMessageDeques.get(messageChannelId).drainDequeIntoList(amount);
        int lastIndex = list.size()-1;
        String oldestMessageId = list.get(lastIndex).getId();
        List<CompletableFuture<Void>> completableFutureList = channel.purgeMessages(list);
        completeInFuture(completableFutureList);
        return oldestMessageId;
    }
    private static void completeInFuture(List<CompletableFuture<Void>> futures){
        futures.forEach(future -> future.completeExceptionally(new Throwable("Insufficient permissions to purge?")));
    }

    protected static void linuxRequest(){
        if(OS.toLowerCase(Locale.ENGLISH).startsWith("win")){
            actions.messageChannel(messageEvent.getTextChannel(),"Host running windows");
            return;
        }
        if(!isAuthorAuthorized()){
            return;
        }
        String command = messageText.substring(messageText.indexOf("$") + 1);
        Process procBuilder = null;
        try{
            procBuilder = run.exec(command);
            procBuilder.waitFor(3, TimeUnit.SECONDS);
        }catch (IOException | InterruptedException ioException){
            ioException.printStackTrace();
            System.err.println("Execution error for command: " + command);
        }
        if (procBuilder == null){
            System.err.println("Process is null");
            return;
        }
        InputStream inputStream = procBuilder.getInputStream();

        //InputStream errorStream = procBuilder.getErrorStream();
        String stringedStream = streamToString(50_000, inputStream);
        if (stringedStream != null){
            actions.sendAsMessageBlock(messageEvent.getTextChannel(), stringedStream);
            try{
                inputStream.close();
            }catch (IOException ioException){
                ioException.printStackTrace();
            }
            inputStream = null;
            stringedStream = null;
        }
        System.out.println("Finished request");
    }

    private static void sleep(int time){
        try{
            Thread.sleep(time);
        }catch (InterruptedException iExc){}
    }

    public static String streamToString(int initialSize, InputStream inputStream){
        String output;
        byte [] buffer = new byte[initialSize];
        try {
            int offset = 0;
            while (inputStream.available() != 0) {
                int available = inputStream.available();
                if(available + offset < buffer.length){
                    int currentRead = inputStream.read(buffer, offset, available);
                    offset += currentRead;
                }else{
                    byte [] tempBuffer = new byte[buffer.length<<1];
                    System.arraycopy(buffer,0,tempBuffer,0,offset);
                    buffer = tempBuffer;
                    tempBuffer = null;
                }
            }
            output = bytesToStr(buffer,offset);
            return output;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("Stream closed?");
        }
        return null;
    }
    private static String bytesToStr(byte [] bytes, int offset){
        char [] charArr = new char[offset];
        for(int i = 0; i<offset; i++){
            charArr[i] = (char)bytes[i];
        }
        return new String(charArr);
    }
    //TODO not implemented
    private static void fileHashRequest(){
        //>file sha256 <url>
    }

    protected static void stringHashRequest(){
        String hashedResult, textToHash;
        int whitespace = messageText.indexOf(' ', PREFIX_OFFSET);
        try{
            if(whitespace == -1){
                System.out.println("No whitespace or was trailing");
                return;
            }
            whitespace++;
            textToHash = messageText.substring(whitespace);
            hashedResult = Hasher.hash(textToHash, commandName);
        }catch (IndexOutOfBoundsException outOfBoundsExc){
            System.err.println("Hashing failed");
            return;
        }
        if(hashedResult != null){
            messageEvent.getChannel().sendMessage(hashedResult).queue();
        }
    }

    public static Youtube getYoutube(){
        return youtube;
    }
    public static void genTokenRequest(){
        actions.messageChannel(messageEvent.getTextChannel(),PseudoBotTokenGenerator.generateBotToken());
    }
    public static void GCRequest(){
        System.gc();
    }
    public static void clearSongsRequest(){
        AudioPlayer.clearAudioTracksFromMemory();
    }
    public static void httpCatRequest(){
        final String httpCatAddress = "https://http.cat/";
        try{
            Integer.parseInt(commandArgs);
        }catch (NumberFormatException nfExc){
            return;
        }
        actions.messageChannel(messageChannelId,httpCatAddress + commandArgs);
    }

    public static void banRequest(){
        if(!isAuthorAuthorized()){
            return;
        }
        if(messageText.length() > PREFIX_OFFSET + USER_ID_LENGTH + 3){
            User userToBan;
            try{
                userToBan = jdaInterface.retrieveUserById(commandArgs).complete();
            }catch (IllegalArgumentException illegalArgExc){
                return;
            }
            boolean res = actions.banUser(userToBan, messageEvent.getGuild());
            if(res){
                actions.messageChannel(messageChannelId,"User unbanned :skull:");
            }
        }
    }

    public static void unbanRequest(){
        if(!isAuthorAuthorized()){
            return;
        }
        if(messageText.length() > PREFIX_OFFSET + USER_ID_LENGTH + 5){
            User userToBan;
            try{
                userToBan = jdaInterface.retrieveUserById(commandArgs).complete();
            }catch (IllegalArgumentException illegalArgExc){
                return;
            }
            boolean res = actions.unbanUser(userToBan, messageEvent.getGuild());
            if(res){
                actions.messageChannel(messageChannelId,"User unbanned :right_facing_fist:");
            }
        }
    }

    public static void lengthRequest(){
        //len
        if(messageText.length() > PREFIX_OFFSET + 3){
            actions.messageChannel(messageEvent.getTextChannel(), "``" + commandArgs.length() +"``");
        }
    }

    //TODO
    public static void auditLogRequest(){
        if(!isAuthorAuthorized()){
            return;
        }

    }

    public static void test(){
        long id1 = 943646128082133032L;
        long id2 = 944235200668377161L;
        Guild thisGuild = messageEvent.getGuild();
        User userToUnban1 = jdaInterface.retrieveUserById(id1).complete();
        User userToUnban2 = jdaInterface.retrieveUserById(id2).complete();
        try{
            thisGuild.retrieveBanById(userToUnban1.getId()).complete();
            thisGuild.unban(userToUnban1).queue();
        }catch (Throwable throwable1){
            System.err.println("User is not banned");
        }
        try{
            thisGuild.retrieveBanById(userToUnban2.getId()).complete();
            thisGuild.unban(userToUnban2).queue();
        }catch (Throwable throwable2){
            System.err.println("User is not banned");
        }

    }
    private static boolean isAuthorAuthorized(){
        return Bot.AUTHORIZED_USERS.contains(messageEvent.getAuthor().getIdLong());
    }
}
