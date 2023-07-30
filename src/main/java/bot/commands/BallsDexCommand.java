package bot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.ArrayList;
import java.util.List;

public class BallsDexCommand extends Command{
    private static final int LOWEST_CHANCE = 40, HIGHEST_CHANCE = 55, RETRIEVAL_LIMIT = 500;
    private static final long BALLS_DEX_ID = 999736048596816014L, WORLD_DEX_ID = 1073275888466145370L;
    private MessageChannelUnion channel;
    private Message requestMessage;
    public final List<String> discoveredBalls = new ArrayList<>();
    public BallsDexCommand(String... aliases){
        super(aliases);
        description = "Estimates the percentage to reach the goal in a worst and best case scenario " +
                "based on time from the last drop\n" +
                "Execute in the drop channel. Alternatively prints discovered balls";
        usage = "balls `message_id`\n" +
                "balls discovered\n" +
                "worlddex `message_id`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        channel = message.getChannel();
        requestMessage = message.getMessage();
        Message refMessage;
        if(args.length == 1 && args[0].equals("discovered")){
            actions.messageChannel(channel, discoveredBalls.toString());
            return;
        }
        if (args.length == 0){
            if (commandName.equals("balls")){
                refMessage = searchForMessage(BALLS_DEX_ID);
            }else if (commandName.equals("worlddex")){
                refMessage = searchForMessage(WORLD_DEX_ID);
            }else{
                return;
            }
        }else{
            refMessage = retrieveMessageById(args[0]);
        }

        if (refMessage == null){
            actions.messageChannel(channel, "Reference message not found");
            return;
        }


        long secondsPassed = requestMessage.getTimeCreated().minusSeconds(
                refMessage.getTimeCreated().toEpochSecond()
        ).toEpochSecond();
        int minutes = (int) (secondsPassed / 60);
        double reduction = (multiplier(message.getGuild().getMemberCount()) * minutes);
        double worstChance = HIGHEST_CHANCE - reduction;
        double bestChance = LOWEST_CHANCE - reduction;
        sendCompletionMessage(worstChance, bestChance, minutes);
    }

    private Message searchForMessage(long botId){
        User user = jda.retrieveUserById(botId).complete();
        try{
            requestMessage.getGuild().retrieveMember(user).complete();
        }catch (ErrorResponseException e){
            actions.messageChannel(channel, "Bot not found in the server");
            return null;
        }

        MessageHistory history = MessageHistory.getHistoryBefore(channel, requestMessage.getId())
                .limit(100)
                .complete();
        for (int i = 0; i < RETRIEVAL_LIMIT; i += 100){
            List<Message> messages = history.getRetrievedHistory();
            for (Message msg : messages){
                if (msg.getAuthor().getIdLong() == botId && msg.getContentRaw().startsWith("A wild")){
                    return msg;
                }
            }
            history.retrievePast(100).complete();
        }

        return null;
    }

    private Message retrieveMessageById(String messageId){
        if (messageId.length() < 17 || messageId.length() > 19){
            return null;
        }

        Long msgId = parseLong(messageId);
        if (msgId == null){
            return null;
        }

        Message dropMessage = null;
        try{
            dropMessage = channel.retrieveMessageById(msgId).complete();
        }catch (ErrorResponseException e){
            actions.messageChannel(channel, e.getMessage());
        }
        return dropMessage;
    }

    private void sendCompletionMessage(double worstChance, double bestChance, int minutes){
        boolean worstComplete = 1.25 >= worstChance;
        boolean bestComplete = 1.25 >= bestChance;
        String msg = "Minutes passed: " + minutes + '\n' +
                "Worst chance: " + (worstComplete ? "100" : asPercentage(1.25 / worstChance)) + "%\n" +
                "Best chance: " + (bestComplete ? "100" : asPercentage(1.25 / bestChance)) + "%\n";
        actions.messageChannel(channel, msg);
    }

    private static String asPercentage(double d){
        return String.format("%.2f", d * 100);
    }

    private static double multiplier(int memberCount){
        if (memberCount < 5){
            return 0.1;
        }else if (memberCount < 100){
            return 0.8;
        }else if (memberCount < 1000){
            return 0.5;
        }else{
            return 0.2;
        }
    }

    private static Long parseLong(String num){
        try{
            return Long.parseLong(num);
        }catch (NumberFormatException e){
            return null;
        }
    }
}
