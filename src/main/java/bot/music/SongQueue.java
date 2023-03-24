package bot.music;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class SongQueue{
    private final Queue<String> playlist;
    public SongQueue(){
        playlist = new LinkedList<>();
    }

    public boolean append(String track){
        return playlist.add(track);
    }

    public void skip(){
        if (playlist.isEmpty()){
            return;
        }
        playlist.remove();
    }

    public String take(){
        if (playlist.isEmpty()){
            return "";
        }
        return playlist.remove();
    }
    public void removeElementAt(int index){
        int[] ind = new int[1];
        Iterator<String> it = playlist.iterator();
        while(it.hasNext()){
            if(index == ind[0]++){
                it.remove();
            }
        }
    }

    public boolean isEmpty(){
        return playlist.isEmpty();
    }

    public int size(){
        return playlist.size();
    }

    public void clear(){
        playlist.clear();
    }

    @Override
    public String toString(){
        StringBuilder songs = new StringBuilder();
        int[] ind = new int[1];
        playlist.forEach(audioTrack ->
                songs.append(ind[0]++).append(". ").append(audioTrack).append("\n"));
        return songs.toString();
    }
}
