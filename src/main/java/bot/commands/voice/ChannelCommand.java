package bot.commands.voice;

import bot.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

public class ChannelCommand extends Command{
    private static final int USER_ID_LEN = 18;

    private final boolean ALLOW_BANS = true;
    private final int LOWER_SIZE_LIMIT = 3, UPPER_SIZE_LIMIT = 99;
    //private final Duration CHANNEL_TIMEOUT = Duration.ofHours(8);
    private final Duration CHANNEL_TIMEOUT = Duration.ofSeconds(20);
    private final HashMap<Long, VoiceChannel> membersToChannels = new HashMap<>();

    public ChannelCommand(String... aliases){
        super(aliases);
        description = "Manages custom user channels\n" +
        "Size limits: " + LOWER_SIZE_LIMIT + "-" + UPPER_SIZE_LIMIT + "\n" +
        "A channel can be renamed twice before 429 is encountered (delete vc or retry after 10 min)";
        usage = "vc create `name`\n" +
                "vc create\n" +
                "vc ban `user_id`\n" +
                "vc ban @mention\n" +
                "vc size `size`\n" +
                "vc rename `name`\n" +
                "vc delete";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(message == null || args.length == 0 || message.getMember() == null){
            return;
        }

        Guild guild = message.getGuild();
        Member member = message.getMember();
        VoiceChannel vc = membersToChannels.get(member.getIdLong());

        switch (args[0]){
            case "create":
                if(vc != null){
                    actions.sendAsMessageBlock(message.getChannel(), "Channel already exists");
                    return;
                }
                String name = args.length == 1 ? member.getEffectiveName() + "'s channel" : args[1];
                VoiceChannel customChannel = guild.createVoiceChannel(name).complete();
                membersToChannels.put(member.getIdLong(), customChannel);
                break;
            case "ban":
            case "exclude":
                if(!ALLOW_BANS){
                    actions.sendAsMessageBlock(message.getChannel(), "Banning is disabled");
                    return;
                }
                if(vc == null){
                    actions.sendAsMessageBlock(message.getChannel(), "Create a channel first");
                    return;
                }

                if(args.length == 1){
                    return;
                }
                List<Member> allMentions = message.getMessage().getMentions().getMembers();

                long targetId;
                if(allMentions.size() == 0){
                    if(args[1].length() != USER_ID_LEN){
                        return;
                    }
                    Long id = castToLong(args[1]);
                    if(id == null){
                        return;
                    }
                    VoiceChannelManager vcManager = vc.getManager();
                    vcManager.putMemberPermissionOverride(id, 0, Permission.VOICE_CONNECT.getRawValue()).queue();
                    targetId = id;
                    actions.sendAsMessageBlock(message.getChannel(), id + " excluded");
                }else{
                    Member theMentioned = allMentions.get(0);
                    VoiceChannelManager vcManager = vc.getManager();
                    vcManager.putMemberPermissionOverride(theMentioned.getIdLong(), 0, Permission.VOICE_CONNECT.getRawValue()).queue();
                    targetId = theMentioned.getIdLong();
                    actions.sendAsMessageBlock(message.getChannel(), theMentioned.getEffectiveName() + " excluded");
                }
                List<Member> participants = vc.getMembers();
                for(Member participant : participants){
                    if(participant.getIdLong() == targetId){
                        guild.kickVoiceMember(participant).queue();
                    }
                }

                break;
            case "resize":
            case "limit":
            case "size":
                if(args.length == 1){
                    return;
                }
                if(vc == null){
                    actions.sendAsMessageBlock(message.getChannel(), "Create a channel first");
                    return;
                }

                Integer limit = castToInt(args[1]);
                if(limit == null){
                    actions.sendAsMessageBlock(message.getChannel(),
                            "Provide a number in range " + LOWER_SIZE_LIMIT + '-' + UPPER_SIZE_LIMIT);
                    return;
                }
                if(limit < LOWER_SIZE_LIMIT || limit > UPPER_SIZE_LIMIT){
                    actions.sendAsMessageBlock(message.getChannel(),
                            "Size range: " + LOWER_SIZE_LIMIT + '-' + UPPER_SIZE_LIMIT);
                    return;
                }
                VoiceChannelManager vcManager = vc.getManager();
                vcManager.setUserLimit(limit).queue();
                break;
            case "rename":
                if(args.length == 1){
                    return;
                }
                if(vc == null){
                    actions.sendAsMessageBlock(message.getChannel(), "Create a channel first");
                    return;
                }
                String vcName = args[1];
                if(!validateChannelName(args[1])){
                    actions.sendAsMessageBlock(message.getChannel(), "Max 100 characters, non-empty");
                    return;
                }
                vcManager = vc.getManager();
                vcManager.setName(vcName).queue();
                break;
            case "delete":
            case "remove":
                if(vc == null){
                    actions.sendAsMessageBlock(message.getChannel(), "Nothing to delete");
                    return;
                }
                vc.delete().queue();
                membersToChannels.remove(member.getIdLong());
                break;
            default:
                break;
        }
    }

    private static Integer castToInt(String num){
        try{
            return Integer.parseInt(num);
        }catch (NumberFormatException e){
            return null;
        }
    }

    private static Long castToLong(String num){
        try{
            return Long.parseLong(num);
        }catch (NumberFormatException e){
            return null;
        }
    }

    private static boolean validateChannelName(String name){
        if(name == null || name.isEmpty()){
            return false;
        }
        if(name.length() > 100){
            return false;
        }
        for (int i = 0; i < name.length(); i++){
            if(name.charAt(i) != ' '){
                return true;
            }
        }
        return false;
    }
}
