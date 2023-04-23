package bot.utilities;

import java.util.*;

public class RandomString{
    private static final Random random = new Random();

    public final char[] set;
    public final int setLength;

    public RandomString(char[] set){
        if(set.length < 1){
            throw new IllegalArgumentException("Set must have at least one element");
        }
        this.set = set;
        this.setLength = set.length;
    }
    public RandomString(boolean a_to_z, boolean A_to_Z, boolean digits){
        this(a_to_z, A_to_Z, digits, new char[0]);
    }
    public RandomString(boolean a_to_z, boolean A_to_Z, boolean digits, char... more){
        if(!a_to_z && !A_to_Z && !digits && more.length < 1){
            throw new IllegalArgumentException("Set must have at least one element");
        }
        List<Character> characters = new ArrayList<>();
        if(a_to_z){
            for (int i = 97; i < 123; i++){
                characters.add((char) i);
            }
        }
        if(A_to_Z){
            for (int i = 65; i < 91; i++){
                characters.add((char) i);
            }
        }
        if(digits){
            for (int i = 48; i < 58; i++){
                characters.add((char) i);
            }
        }
        for(char c : more){
            characters.add(c);
        }
        set = new char[characters.size()];
        for (int i = 0; i < characters.size(); i++){
            set[i] = characters.get(i);
        }
        this.setLength = set.length;
    }

    public String[] generateRandomStrings(int quantity, int length){
        if(quantity < 1){
            throw new IllegalArgumentException("The quantity must be at least 1, given: " + quantity);
        }
        if(quantity > Integer.MAX_VALUE - 4){
            throw new IllegalArgumentException("The array is too large to allocate on the JVM, given: " + quantity);
        }
        String[] arr = new String[quantity];
        for (int i = 0; i < quantity; i++){
            StringBuilder str = new StringBuilder();
            for (int j = 0; j < length; j++){
                int index = random.nextInt(setLength);
                str.append(set[index]);
            }
            arr[i] = str.toString();
            //toString() makes a copy
            str.setLength(0);
        }
        return arr;
    }

    public String nextString(int length){
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < length; i++){
            int index = random.nextInt(setLength);
            str.append(set[index]);
        }
        return str.toString();
    }
}

