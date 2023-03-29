package bot.deskort;

import bot.deskort.commands.MemoryCommand;
import bot.music.AudioPlayer;
import bot.utilities.LeaverTimer;
import bot.utilities.MessageDeque;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class EventsListener extends ListenerAdapter{
    private final HashMap<Guild, LeaverTimer> guildsToTimers;
    private final MessageProcessor messageProcessor;
    private Guild guildOfOrigin;
    private MessageReceivedEvent messageEvent;
    private String messageText;

    public EventsListener(){
        List<Guild> guilds = Bot.getJDAInterface().getGuilds();
        guildsToTimers = new HashMap<>(guilds.size() + 1, 1);
        for (Guild guild : guilds){
            guildsToTimers.put(guild, new LeaverTimer(guild.getAudioManager()));
        }
        System.out.println("Leaver timer is initialized");
        messageProcessor = MessageProcessor.get();
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent messageEdited) {
        String editedRawContent = messageEdited.getMessage().getContentRaw();
        String authorName = messageEdited.getAuthor().getAsTag();
        boolean isBot = messageEdited.getAuthor().isBot();
        if(editedRawContent.length() > 0){
            System.out.println(authorName + "(bot:" + isBot + ")" +
                    " edited their message in [" + messageEdited.getChannel().getName() + "] to " + editedRawContent);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent messageEvent){
        this.messageEvent = messageEvent;
        this.messageText = messageEvent.getMessage().getContentRaw();

        printReceivedMessage();
        long channelId = messageEvent.getChannel().getIdLong();
        messageProcessor.processMessage(messageEvent, channelId);
    }
    @Override
    public void onGuildUnban(GuildUnbanEvent unbanEvent){
        System.out.println("Member ["
                + unbanEvent.getUser().getName()
                + "] was unbanned");
    }
    @Override
    public void onChannelDelete(ChannelDeleteEvent channelDelete) {
        long deletedChannelId = channelDelete.getChannel().getIdLong();
        HashMap<Long, MessageDeque> map = messageProcessor.channelIdsToMessageDeques;
        if(map.containsKey(deletedChannelId)){
            map.remove(deletedChannelId);
            System.out.println("Deleted non-existent channel to prevent memory leaks");
        }
    }
    @Override
    public void onGenericGuildVoice(GenericGuildVoiceEvent voiceEvent) {
        guildOfOrigin = voiceEvent.getGuild();

        AudioManager thisAudioManager = guildOfOrigin.getAudioManager();
        //voice channel presence events

        if (voiceEvent instanceof GuildVoiceUpdateEvent){
            scheduleTimerForTheFuture(thisAudioManager);
            GuildVoiceUpdateEvent updateEvent = (GuildVoiceUpdateEvent) voiceEvent;
            boolean moved = true;
            if(updateEvent.getNewValue() == null){
                moved = false;
                String channelName = updateEvent.getOldValue() == null ? "unknown" : updateEvent.getOldValue().getName();
                System.out.println(toString(voiceEvent.getMember()) + " left " + channelName);
            }
            if(updateEvent.getOldValue() == null){
                moved = false;
                System.out.println(toString(voiceEvent.getMember()) + " joined " + updateEvent.getNewValue().getName());
            }
            if(moved){
                System.out.println(toString(voiceEvent.getMember()) + " moved vc.");
            }
        }
    }

    private void moveBack(GenericGuildVoiceEvent voiceEvent){
        Member member = voiceEvent.getMember();
        AudioChannel ac = ((GuildVoiceUpdateEvent) voiceEvent).getChannelLeft();
        guildOfOrigin.moveVoiceMember(member, ac).queueAfter(1, TimeUnit.SECONDS);
    }

    private String toString(Member member){
        return member.getUser().getAsTag();
    }
    private void scheduleTimerForTheFuture(final AudioManager audioManager){
        //connection is delayed to
        TimerTask futureTask = new TimerTask(){
            @Override
            public void run(){
                if(audioManager.isConnected()){
                    @SuppressWarnings("all")
                    List<Member> connectedMembers = audioManager.getConnectedChannel().getMembers();
                    LeaverTimer leaverTimer = guildsToTimers.get(guildOfOrigin);
                    //bot alone
                    if(connectedMembers.size() == 1){
                        System.out.println("Scheduled");
                        leaverTimer.schedule();
                    }
                    //bot not alone
                    else{
                        if(leaverTimer.isScheduled()){
                            leaverTimer.cancel();
                        }
                    }
                }
            }
        };
        new Timer().schedule(futureTask, 1500);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent clickEvent) {
        String buttonId = clickEvent.getComponentId();
        switch (buttonId){
            case "clrsongs":
                AudioPlayer.clearAudioTracksFromMemory();
                break;
            case "gc":
                System.gc();
                try{
                    Thread.sleep(100);
                }catch (InterruptedException ignored){
                }
                break;
            case "refresh":
                break;
            default:
                System.out.println("Foreign button clicked");
                return;
        }
        MessageEmbed memoryEmbed = MemoryCommand.createMemoryEmbed();
        clickEvent.editMessageEmbeds().setEmbeds(memoryEmbed).queue();
        //clickEvent.getHook().editOriginalEmbeds(memoryEmbed).queue();
    }

    private void printReceivedMessage(){
        String authorName = messageEvent.getAuthor().getName(), channelName = messageEvent.getChannel().getName();
        if(messageText.length() == 0){
            messageText = attachmentsToString();
        }
        System.out.println("[" +channelName+ "]  " + authorName + ": " + messageText);
    }

    private String attachmentsToString(){
        List<Message.Attachment> attachments = messageEvent.getMessage().getAttachments();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < attachments.size(); i++){
            Message.Attachment attachment = attachments.get(i);
            str.append(attachment.getUrl());
            if(i != attachments.size() - 1){
                str.append(' ');
            }
        }
        return str.toString();
    }

    public void sendCommunistMessage(){
        MessageChannel channel = messageEvent.getChannel();
        String msgText = "ATTENTION CITIZEN! 市民请注意! ⣿⣿⣿⣿⣿⠟⠋⠄⠄⠄⠄⠄⠄⠄⢁⠈⢻⢿⣿⣿⣿⣿⣿⣿⣿ ⣿⣿⣿⣿⣿⠃⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠈⡀⠭⢿⣿⣿⣿⣿ ⣿⣿⣿⣿⡟⠄⢀⣾⣿⣿⣿⣷⣶⣿⣷⣶⣶⡆⠄⠄⠄⣿⣿⣿⣿ ⣿⣿⣿⣿⡇⢀⣼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣧⠄⠄⢸⣿⣿⣿⣿ ⣿⣿⣿⣿⣇⣼⣿⣿⠿⠶⠙⣿⡟⠡⣴⣿⣽⣿⣧⠄⢸⣿⣿⣿⣿ ⣿⣿⣿⣿⣿⣾⣿⣿⣟⣭⣾⣿⣷⣶⣶⣴⣶⣿⣿⢄⣿⣿⣿⣿⣿ ⣿⣿⣿⣿⣿⣿⣿⣿⡟⣩⣿⣿⣿⡏⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿ ⣿⣿⣿⣿⣿⣿⣹⡋⠘⠷⣦⣀⣠⡶⠁⠈⠁⠄⣿⣿⣿⣿⣿⣿⣿ ⣿⣿⣿⣿⣿⣿⣍⠃⣴⣶⡔⠒⠄⣠⢀⠄⠄⠄⡨⣿⣿⣿⣿⣿⣿ ⣿⣿⣿⣿⣿⣿⣿⣦⡘⠿⣷⣿⠿⠟⠃⠄⠄⣠⡇⠈⠻⣿⣿⣿⣿ ⣿⣿⣿⣿⡿⠟⠋⢁⣷⣠⠄⠄⠄⠄⣀⣠⣾⡟⠄⠄⠄⠄⠉⠙⠻ ⡿⠟⠋⠁⠄⠄⠄⢸⣿⣿⡯⢓⣴⣾⣿⣿⡟⠄⠄⠄⠄⠄⠄⠄⠄ ⠄⠄⠄⠄⠄⠄⠄⣿⡟⣷⠄⠹⣿⣿⣿⡿⠁⠄⠄⠄⠄⠄⠄⠄⠄ ATTENTION CITIZEN! 市民请注意! This is the Central Intelligentsia of the Chinese Communist Party. 您的 Internet 浏览器历史记录和活动引起了我们的注意 YOUR INTERNET ACTIVITY HAS ATTRACTED OUR ATTENTION. 志們注意了 you have been found protesting in the subreddit!!!!! 這是通知你，你必須 我們將接管台灣 serious crime 以及世界其他地方 100 social credits have been deducted from your account 這對我們所有未來的下屬來說都是一個重要的機會 stop the protest immediately 立即加入我們的宣傳活動，提前獲得 do not do this again! 不要再这样做! if you do not hesitate, more social credits ( -11115 social credits )will be subtracted from your profile, resulting in the subtraction of ration supplies. (由人民供应部重新分配 ccp) you’ll also be sent into a re-education camp in the xinjiang uyghur autonomous zone. 为党争光! Glory to the CCP! I am a bot, and this action was performed automatically. Please contact the moderators of this subreddit if you have any questions or concerns.";
        channel.sendMessage(msgText).queue();
    }

    final static String [] RESPONSES_CHINESE = {
            "Pinchiren",
            "早上好中国 现在我有冰淇淋 我很喜欢冰淇淋 但是 《速度与激情9》 比冰淇淋",
            "《速度与激情…",
            "《速度与激情9》 我最喜欢 所以现在 音乐时间 准备 1 2 3 两个礼拜以后",
            "两个礼拜以后",
            "《速度与激情9》 不要忘记 不要错过 去电影院 看",
            "《速度与激情9》 因为非常好电影 动作非常好 差不多一样 冰淇淋 再见",
            "chinj cheng hanji",
    };
}
