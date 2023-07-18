package bot.core.emergency;

import bot.core.Bot;
import bot.utilities.jda.Actions;
import bot.utilities.jda.AuditLog;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
    Exists for the contingency of a breach
    Intended to monitor suspicious behavior
 */
public class EmergencyListener extends ListenerAdapter{

    public static final int TRIGGER_DELAY_MS = 5000;
    public static final int EVENT_THRESHOLD = 3;

    private static final HashMap<Long, EventQueue> userIdsToEvents = new HashMap<>();

    public volatile boolean awaitKick = false;
    public volatile HashMap<Guild, AuditLogEntry> guildsToLastKicks = null;
    public boolean isReady = false;

    public EmergencyListener(JDABuilder jdaBuilder){
        //for these to work, SERVER MEMBERS INTENT must be enabled in the 'Bot' tab in discord developer panel
        jdaBuilder.enableIntents(GatewayIntent.GUILD_BANS);
        jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
    }

    @Override
    public void onReady(@NotNull ReadyEvent readyEvent) {
        isReady = true;
        List<Guild> connectedGuilds = Bot.getJDAInterface().getGuilds();
        guildsToLastKicks = new HashMap<>(connectedGuilds.size());

        //to distinguish leave from a kick, the server must have at least one kick entry
        //this solution accounts for all servers the bot is connected to
        for(Guild guild : connectedGuilds){
            List<AuditLogEntry> entryList = AuditLog.retrieveFromAuditLog(ActionType.KICK,1, guild);
            if(entryList == null || entryList.size() == 0){
                continue;
            }
            guildsToLastKicks.put(guild, entryList.get(0));
        }
    }

    @Override
    public void onGenericEvent(@NotNull GenericEvent anyEvent) {
        if(!isReady){
            return;
        }

        //timestamp is made when an event is received
        //much more accurate and reliable than audit log entry timestamps which tend to be displaced in time by approximately 10 seconds
        long eventTimestamp = System.currentTimeMillis();
        if(anyEvent instanceof ChannelDeleteEvent){

            ChannelDeleteEvent channelDeleteEvent = (ChannelDeleteEvent)anyEvent;
            List<AuditLogEntry> channelDeletions = AuditLog.retrieveFromAuditLog(ActionType.CHANNEL_DELETE,1, channelDeleteEvent.getGuild());
            AuditLogEntry deletionEntry = channelDeletions.get(0);
            User userResponsible = deletionEntry.getUser();
            System.out.println("Channel [" + channelDeleteEvent.getChannel().getName() + "] was deleted by " + Objects.requireNonNull(userResponsible).getName());

            if(channelDeleteEvent.getChannelType() != ChannelType.TEXT){
                return;
            }
            resolveEvent(deletionEntry, eventTimestamp);

        }else if(anyEvent instanceof GuildBanEvent){
            //ban events subsequently trigger kick events
            awaitKick = true;

            GuildBanEvent memberBannedEvent = (GuildBanEvent) anyEvent;
            List<AuditLogEntry> bans = AuditLog.retrieveFromAuditLog(ActionType.BAN,1, memberBannedEvent.getGuild());
            AuditLogEntry banEntry = bans.get(0);
            User userResponsible = banEntry.getUser();
            System.out.println("Member [" + memberBannedEvent.getUser().getName() + "] was banned by " + Objects.requireNonNull(userResponsible).getName());

            resolveEvent(userResponsible, eventTimestamp);

        }else if(anyEvent instanceof GuildMemberRemoveEvent){
            //will trigger on ban/kick/leave - supposed to differentiate between bans and kicks
            if(awaitKick) {
                awaitKick = false;
                System.out.println("Caught subsequent kick");
                return;
            }

            GuildMemberRemoveEvent kickOrLeaveEvent = (GuildMemberRemoveEvent) anyEvent;
            List<AuditLogEntry> kicks = AuditLog.retrieveFromAuditLog(ActionType.KICK,1, kickOrLeaveEvent.getGuild());
            AuditLogEntry kickEntry = kicks.get(0);
            //if true, member left a server (wasn't kicked)
            AuditLogEntry lastKickEntry = guildsToLastKicks.get(kickOrLeaveEvent.getGuild());
            if(kickEntry.equals(lastKickEntry)){
                System.out.println("Member ["
                        + kickOrLeaveEvent.getUser().getName()
                        + "] left the server");
                return;
            }
            guildsToLastKicks.put(kickEntry.getGuild(), kickEntry);
            User userResponsible = kickEntry.getUser();
            System.out.println("Member ["
                    + kickOrLeaveEvent.getUser().getName()
                    + "] was kicked by "
                    + Objects.requireNonNull(userResponsible).getName());

            resolveEvent(kickEntry, eventTimestamp);
        }
    }

    private void resolveEvent(User responsibleUser, long currentTimestamp){
        long userResponsibleId = responsibleUser.getIdLong();
        if(userResponsibleId == Bot.BOT_ID){
            return;
        }
        EventQueue eventQueue;
        if(userIdsToEvents.containsKey(userResponsibleId)){
            eventQueue = userIdsToEvents.get(userResponsibleId);
        }
        else{
            eventQueue = new EventQueue(EVENT_THRESHOLD);
            userIdsToEvents.put(userResponsibleId, eventQueue);
        }
        eventQueue.append(currentTimestamp);
        if(eventQueue.isAlarmable(TRIGGER_DELAY_MS)){
            messageAuthorizedUsers(responsibleUser);
            System.out.println("---TRIGGERED ALARM---");
        }
    }
    private void resolveEvent(AuditLogEntry auditEntry, long currentTimestamp){
        User userResponsible = auditEntry.getUser();
        if(userResponsible == null){
            return;
        }
        resolveEvent(userResponsible, currentTimestamp);
    }

    private void messageAuthorizedUsers(User responsibleUser){
        String emergencyMessage = "Emergency triggered"
                + " by "  + responsibleUser.getAsTag()
                + " (id: " + responsibleUser.getIdLong() + ")";
        Actions actions = Bot.getActions();
        for (long id : Bot.AUTHORIZED_USERS){
            actions.messageUser(id, emergencyMessage);
        }
        
    }
}
