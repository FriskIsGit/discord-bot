package bot.utilities;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher{
    static String [] splitStrInto(String str, int parts){
        int totalLen = str.length();
        int baseParseLen = totalLen/parts;
        String [] parsedStr = new String[totalLen/(double)(parts) % 1 == 0 ? parts : parts+1];
        for(int i = 0, p = 0; p<totalLen; p+=baseParseLen, i++){
            parsedStr[i] = str.substring(p, Math.min(totalLen,p+baseParseLen));
        }
        return parsedStr;
    }

    public static String sha256(String text){
        MessageDigest msgDigest;
        try{
            msgDigest = MessageDigest.getInstance("SHA-256");
        }catch (NoSuchAlgorithmException nosaExc){
            return null;
        }
        byte [] bytes = text.getBytes(StandardCharsets.UTF_8);
        byte [] hashedBytes = msgDigest.digest(bytes);
        //signum representation - formula [for i = 0; byteValue * 256^i]
        BigInteger number = new BigInteger(1, hashedBytes);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        return new String(hexString);
    }

    public static String anySHA(String text, String algorithm){
        MessageDigest msgDigest;
        try{
            msgDigest = MessageDigest.getInstance(algorithm);
        }catch (NoSuchAlgorithmException nosaExc){
            return null;
        }
        byte [] bytes = text.getBytes(StandardCharsets.UTF_8);
        byte [] hashedBytes = msgDigest.digest(bytes);
        //signum representation - formula [for i = 0; byteValue * 256^i]
        BigInteger number = new BigInteger(1, hashedBytes);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        return new String(hexString);
    }
}
