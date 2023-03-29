package bot.utilities;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

//to be expanded with 'auditlog' command available to administrators
public class AuditLog{
    public static List<AuditLogEntry> retrieveFromAuditLog(ActionType actionType, int limit, Guild guildOfOrigin){
        return guildOfOrigin
                .retrieveAuditLogs()
                .limit(limit)
                .type(actionType)
                .complete();
    }
    public static List<AuditLogEntry> retrieveFromAuditLog(ActionType actionType, Guild guildOfOrigin){
        return guildOfOrigin
                .retrieveAuditLogs()
                .type(actionType)
                .complete();
    }
    public static List<AuditLogEntry> retrieveAllBans(Guild guildOfOrigin){
        return guildOfOrigin
                .retrieveAuditLogs()
                .type(ActionType.BAN)
                .complete();
    }
    public static List<AuditLogEntry> retrieveAllKicks(Guild guildOfOrigin){
        return guildOfOrigin
                .retrieveAuditLogs()
                .type(ActionType.KICK)
                .complete();
    }

    public static List<AuditLogEntry> retrieveBansAndKicksSimultaneously(int limit, Guild guildOfOrigin){
        AtomicReference<List<AuditLogEntry>> banEntries = new AtomicReference<>();
        AtomicReference<List<AuditLogEntry>> kickEntries = new AtomicReference<>();

        Thread banRetrieval = new Thread(() ->{
            banEntries.set(
                    guildOfOrigin.retrieveAuditLogs()
                            .limit(limit)
                            .type(ActionType.BAN)
                            .complete()
            );
        }
        );
        Thread kicksRetrieval = new Thread(() ->{
            kickEntries.set(
                    guildOfOrigin.retrieveAuditLogs()
                            .limit(limit)
                            .type(ActionType.BAN)
                            .complete()
            );
        }
        );
        banRetrieval.start();
        kicksRetrieval.start();
        try{
            banRetrieval.join();
            kicksRetrieval.join();
        }catch (InterruptedException interruptedExc){
            interruptedExc.printStackTrace();
        }
        List<AuditLogEntry> miscEntries = banEntries.get();
        miscEntries.addAll(kickEntries.get());
        return miscEntries;
    }
    public static ActionType toActionType(String text){
        text = text.toLowerCase(Locale.ENGLISH);
        switch (text){
            case "ban":
            case "bans":
                return ActionType.BAN;
            case "unban":
            case "unbans":
                return ActionType.UNBAN;
            case "kick":
            case "kicks":
                return ActionType.KICK;
            case "move":
            case "moves":
                return ActionType.MEMBER_VOICE_MOVE;
            case "bulk":
            case "bulks":
                return ActionType.MESSAGE_BULK_DELETE;
            case "roleupdate":
            case "role_update":
                return ActionType.MEMBER_ROLE_UPDATE;
            case "channeldelete":
            case "channel_delete":
                return ActionType.CHANNEL_DELETE;
            case "msgdelete":
            case "msgdeleted":
            case "messagedelete":
            case "messagedeleted":
            case "message_delete":
            case "message_deleted":
                return ActionType.MESSAGE_DELETE;
            case "unknown":
                return ActionType.UNKNOWN;
            default:
                return null;
        }
    }
}
