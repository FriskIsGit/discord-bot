package bot.deskort.emergency;

import java.util.Iterator;
import java.util.LinkedList;

public class EventQueue{
    private final LinkedList<Long> queue;
    private int capacity;
    public EventQueue(int capacity){
        queue = new LinkedList<>();
        this.capacity = capacity;
    }

    public void append(Long element){
        if (capacity == queue.size()){
            this.removeLast();
        }
        queue.add(element);
    }

    /**
     * Determines whether time proximity of events in the queue is alarmable
     * Conditions: queue has to be full and time difference between each two events must be greater than delay
     * @param delayMillis in ms between each two events
     * @return true if all events are alarming, false otherwise
     */
    public boolean isAlarmable(long delayMillis){
        int size = size();
        if(size < 2 || size != capacity){
            return false;
        }
        //iterator starting from the latest events
        Iterator<Long> descIt = descendingIterator();
        long left, right = descIt.next();
        while(descIt.hasNext()){
            left = descIt.next();
            if(right - left > delayMillis){
                return false;
            }

            right = left;
        }
        return true;
    }

    /**
     * Determines whether time proximity of events in the queue is alarmable
     * Conditions: time difference between two events must be less than delay but only minNumOfTriggers number of times
     * @param delayMillis in ms between each two events
     * @param minNumOfTriggers number of times, the time difference between two events was less than the specified delay,
     * @return true if is alarming, false otherwise
     */
    public boolean isAlarmable(long delayMillis, int minNumOfTriggers){
        int size = size();
        if(size < 0 || size <= minNumOfTriggers){
            return false;
        }
        //iterator starting from the latest events
        int triggers = 0;
        Iterator<Long> descIt = descendingIterator();
        long left, right = descIt.next();
        while(descIt.hasNext()){
            left = descIt.next();
            if(right - left < delayMillis){
                if(++triggers >= minNumOfTriggers){
                    return true;
                }
            }
            right = left;
        }
        return false;
    }

    public Iterator<Long> iterator(){
        return queue.iterator();
    }
    public Iterator<Long> descendingIterator(){
        return queue.descendingIterator();
    }
    public void resize(int newCapacity){
        this.capacity = newCapacity;
    }
    public int size(){
        return queue.size();
    }
    public int capacity(){
        return capacity;
    }
    //removes and returns the element
    public Long removeLast(){
        return queue.remove(0);
    }
    public void clear(){
        queue.clear();
    }
    @Override
    public String toString(){
        return queue.toString();
    }
}
