package bot.deskort;

import com.sun.jmx.remote.internal.ArrayQueue;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

class MessageQueue extends ArrayQueue<MessageReceivedEvent>{
    private int capacity;
    public MessageQueue(int capacity){
        super(capacity);
        this.capacity = capacity;
    }

    void append(MessageReceivedEvent message){
        if(capacity == this.size()){
            this.remove(0);
        }
        this.add(message);
    }
    public int getCapacity(){
        return capacity;
    }

    @Override
    public void resize(int newCapacity){
       this.resize(newCapacity);
       this.capacity = newCapacity;
    }
    //removes and returns the element
    public MessageReceivedEvent remove(){
        this.remove(0);
        return this.get(0);
    }
    public void streamQueue(){
        int quantity = this.size();
        System.out.println("Cache size: " + quantity);
        StringBuilder messagesBlock = new StringBuilder();
        for (MessageReceivedEvent message : this){
            String author = message.getAuthor().getName();
            String msgContent = message.getMessage().getContentRaw();
            messagesBlock.append(author);
            messagesBlock.append(": ");
            messagesBlock.append(msgContent);
            messagesBlock.append("\n");
        }
        System.out.println(messagesBlock);
    }

}
