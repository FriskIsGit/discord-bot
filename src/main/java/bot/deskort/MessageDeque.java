package bot.deskort;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class MessageDeque extends ArrayDeque<MessageReceivedEvent>{
    private final int maxSize;

    public MessageDeque(int maxNumberOfMessages){
        super();
        this.maxSize = maxNumberOfMessages;
    }
    public int getMaxSize(){
        return maxSize;
    }

    public List<Message> drainDequeIntoList(int amount){
        amount = Math.min(this.size(),amount);
        List<Message> listToPurge = new ArrayList<>(amount);
        for(int i = 0; i<amount; i++){
            listToPurge.add(this.removeLast().getMessage());
        }
        return listToPurge;
    }


    public void print(){
        System.out.println("[========================]");
        this.stream()
                .peek(element -> System.out.println("[" + element.getChannel().getName() + "] " + element.getAuthor().getName() + ": " + element.getMessage().getContentRaw())).count();
        System.out.println("[============ "+ this.size() + " ============]");
    }
    @Override
    public boolean add(@NotNull MessageReceivedEvent msgReceived){
        if(this.maxSize == this.size()){
            removeFirst();
        }
        return super.add(msgReceived);
    }

    //O(N)
    public void removeById(final long messageId){
        Iterator<MessageReceivedEvent> iterator = this.descendingIterator();
        while(iterator.hasNext()){
            MessageReceivedEvent msgReceived = iterator.next();
            if(msgReceived.getMessageIdLong() == messageId){
                msgReceived.getMessage().delete().queue();
                return;
            }
        }
    }
}
