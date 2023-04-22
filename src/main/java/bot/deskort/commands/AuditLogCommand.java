package bot.deskort.commands;

import bot.utilities.jda.AuditLog;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class AuditLogCommand extends Command{
    public AuditLogCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
        description = "Retrieves audit log entries";
        usage = "auditlog `action_type` `limit`\n" +
                "auditlog `action_type`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 0){
            return;
        }
        ActionType actionType = AuditLog.toActionType(args[0]);
        if(actionType == null){
            actions.messageChannel(message.getChannel(), "Action type mismatch: " + args[0]);
            return;
        }
        int limit = 50;
        if(args.length == 2){
            try{
                limit = Integer.parseInt(args[1]);
            }catch (NumberFormatException nfExc){
                return;
            }
        }
        //this command needs improvements for better readability
        List<AuditLogEntry> entryList = AuditLog.retrieveFromAuditLog(actionType, limit, message.getGuild());
        StringBuilder entriesBuilder = new StringBuilder(128);
        entriesBuilder.append("Retrieved ").append(entryList.size()).append(" entries of type ").append(actionType).append('\n');
        for (AuditLogEntry entry : entryList){
            entriesBuilder.append("Approximate time: ").append(entry.getTimeCreated()).append(' ');
            User userResponsible = entry.getUser();
            if(userResponsible == null){
                continue;
            }
            entriesBuilder
                    .append("User responsible: ").append(userResponsible.getAsTag()).append(' ')
                    .append("Target ID: ").append(entry.getTargetId()).append(' ')
                    .append('\n');
        }
        actions.sendAsMessageBlock(message.getChannel(), entriesBuilder.toString());
    }
}
