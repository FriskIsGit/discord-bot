package bot.utilities;

import java.util.Random;

public class PseudoBotTokenGenerator{
    final static Random random = new Random();
    final static int[] LENGTHS = {24,6,27};

    public static void main(String[] args){
        System.out.println(generateBotToken());
    }
    public static String generateBotToken(){
        StringBuilder token = new StringBuilder();
        appendSection(token, LENGTHS[0]);
        token.append('.');
        appendSection(token, LENGTHS[1]);
        token.append('.');
        appendSection(token, LENGTHS[2]);
        if(!Character.isUpperCase(token.charAt(0))){
            token.replace(0,1, String.valueOf((char)(getRandom(65,90))));
        }
        return token.toString();
    }

    private static void appendSection(StringBuilder token, int length){
        char character;
        int offset = 0;
        while(offset < length){
            int characterType = getRandom(1,3);
            switch (characterType){
                //upper case
                default:
                case 1:
                    character = (char)(getRandom(65,90));
                    break;
                //lower case
                case 2:
                    character = (char)(getRandom(97,122));
                    break;
                //number
                case 3:
                    character = (char)(getRandom(48,57));
                    break;
            }
            token.append(character);
            offset++;
        }
    }
    public static int getRandom(int min, int max){
        return random.nextInt(max + 1 - min) + min;
    }
    public static boolean couldBeValid(String token){
        if (token.length() != 59 || !Character.isUpperCase(token.charAt(0))){
            return false;
        }
        char[] arr = token.toCharArray();
        for (int i = 0; i < 59; i++){
            if(arr[i] == '.'){
                if(i != 24 && i != 31){
                    return false;
                }
            }
        }
        return true;
    }
}
