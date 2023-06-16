package bot.deskort;

import bot.deskort.commands.MemoryCommand;
import bot.music.AudioPlayer;
import bot.utilities.jda.LeaverTimer;
import bot.utilities.jda.MessageDeque;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
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
        int newLine = messageText.indexOf('\n');
        if(newLine > -1){
            int remaining = messageText.length() - newLine;
            String more = " (+" + remaining + " more)";
            messageText = messageText.substring(0, newLine) + more;
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
}
