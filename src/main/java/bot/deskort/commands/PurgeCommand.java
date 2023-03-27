package bot.deskort.commands;

import bot.deskort.Bot;
import bot.deskort.MessageDeque;
import bot.deskort.MessageProcessor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PurgeCommand extends Command{
    public final int PURGE_CAP;
    public final MessageProcessor msgProcessor;

    public PurgeCommand(String... aliases){
        super(aliases);
        requiresAuth = true;
        description = "Purges/deletes messages from channel";
        usage = "purge `quantity`";
        msgProcessor = MessageProcessor.get();
        PURGE_CAP = Bot.getConfig().purgeCap;
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length < 1){
            return;
        }

        int amount;
        try{
            amount = Integer.parseInt(args[0]);
        }catch (NumberFormatException nfExc){
            return;
        }

        if(amount > PURGE_CAP || amount<1){
            msgProcessor.deleteRequestMessage(message.getMessage());
            return;
        }
        //include purge request message
        amount++;
        MessageChannelUnion channel = message.getChannel();
        MessageDeque cachedMessages = msgProcessor.channelIdsToMessageDeques.get(channel.getIdLong());
        if(cachedMessages == null){
            actions.messageChannel(channel, "Channel key doesn't exist");
            return;
        }
        int deqAmount = Math.min(cachedMessages.size(), amount);

        String lastMessageId = msgProcessor.popAndPurgeLastMessages(channel, deqAmount);
        amount = amount - deqAmount;

        boolean retrieved = false, exhausted = false;
        long start = -1, end= -1;
        if(amount > 0){
            retrieved = true;
            start = System.currentTimeMillis();

            int maxedHistories = amount/100;
            List<Message> historiesList = new ArrayList<>(maxedHistories + 1);
            for (int h = 0; h < maxedHistories; h++){
                MessageHistory.MessageRetrieveAction retrieveHistoryAction = MessageHistory.getHistoryBefore(channel,lastMessageId).limit(100);
                List<Message> aHistory = retrieveHistoryAction.complete().getRetrievedHistory();
                if(aHistory.size() == 0){
                    exhausted = true;
                    break;
                }
                //almost always lastIndex == 99
                int lastIndex = aHistory.size()-1;
                lastMessageId = aHistory.get(lastIndex).getId();
                historiesList.addAll(aHistory);
            }
            //add leftovers
            if(!exhausted){
                MessageHistory.MessageRetrieveAction retrieveHistoryAction = MessageHistory.getHistoryBefore(channel,lastMessageId).limit(amount%100);
                List<Message> aHistory = retrieveHistoryAction.complete().getRetrievedHistory();
                historiesList.addAll(aHistory);
            }

            List<CompletableFuture<Void>> completableFutureList = channel.purgeMessages(historiesList);
            MessageProcessor.completeInFuture(completableFutureList);

            end = System.currentTimeMillis();
        }

        if(retrieved)
            actions.messageChannel(channel,"Retrieve purge: " + (end-start));

    }
}
