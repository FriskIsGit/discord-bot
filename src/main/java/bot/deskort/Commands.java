package bot.deskort;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Commands{
    final static HashMap<String, RequestFunction> COMMANDS_TO_FUNCTIONS = new HashMap<String, RequestFunction>() {{
        put("clrsongs",    new RequestFunction(MessageProcessor::clearSongsRequest, true));
        put("gc",          new RequestFunction(MessageProcessor::GCRequest,         true));
    }};

    public static String[] doubleTermSplit(String commandText){
        return doubleTermSplit(commandText,0);
    }
    //always returns an array of length 2
    public static String[] doubleTermSplit(String commandText, int fromIndex){
        if(fromIndex < 0 || commandText.length() <= fromIndex){
            return new String[]{"",""};
        }
        ArrayList<String> terms = new ArrayList<>(2);
        char[] arr = commandText.toCharArray();
        boolean lookingForWhitespace = true;
        for (int i = fromIndex; i < arr.length; i++){
            if(lookingForWhitespace && arr[i] == ' '){
                terms.add(commandText.substring(fromIndex, i));
                lookingForWhitespace = false;
            }
            else if(!lookingForWhitespace && arr[i] != ' '){
                terms.add(commandText.substring(i));
                break;
            }
        }
        if(terms.size() == 0){
            return new String[]{commandText.substring(fromIndex), ""};
        }
        if (terms.size() == 1){
            return new String[]{terms.get(0), ""};
        }
        String[] res = new String[2];
        final int[] index = {0};
        terms.forEach((el) -> res[index[0]++] = el);
        return res;
    }

    //returns an array of length at least 1
    public static String[] splitIntoTerms(String text, int fromIndex){
        if(fromIndex < 0 || text.length() <= fromIndex){
            return new String[]{""};
        }
        ArrayList<String> terms = new ArrayList<>(8);
        text = text.trim();
        char[] arr = text.toCharArray();
        boolean expectingWhitespace = true;
        for (int i = fromIndex; i<arr.length; i++){

            if(expectingWhitespace && arr[i] == ' '){
                terms.add(text.substring(fromIndex,i));
                expectingWhitespace = false;
            }else if(!expectingWhitespace && arr[i] != ' '){
                fromIndex = i;
                expectingWhitespace = true;
            }
            if(i == arr.length-1){
                terms.add(text.substring(fromIndex,arr.length));
                break;
            }
        }
        if(terms.size() == 0){
            return new String[]{""};
        }
        String[] res = new String[terms.size()];
        final int[] index = {0};
        terms.forEach((el) -> res[index[0]++] = el);
        return res;
    }

}
