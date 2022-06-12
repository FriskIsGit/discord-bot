package bot.utilities;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Hasher{
    public final static HashMap<String, String> NAMES_TO_ALGORITHMS = new HashMap<String, String>() {{
        put("hash",   "SHA-256");
        put("sha256", "SHA-256");
        put("sha384", "SHA-384");
        put("sha512", "SHA-512");

        put("sha1",   "SHA-1");
        put("md5",    "MD5");
    }};
    public static boolean hasAlgorithm(String alg){
        if(alg.length() > 6){
            return false;
        }
        return NAMES_TO_ALGORITHMS.containsKey(alg);
    }

    static String [] splitStrInto(String str, int parts){
        int totalLen = str.length();
        int baseParseLen = totalLen/parts;
        String [] parsedStr = new String[totalLen/(double)(parts) % 1 == 0 ? parts : parts+1];
        for(int i = 0, p = 0; p<totalLen; p+=baseParseLen, i++){
            parsedStr[i] = str.substring(p, Math.min(totalLen,p+baseParseLen));
        }
        return parsedStr;
    }

    public static String hash(String text, String algorithm){
        MessageDigest msgDigest;
        try{
            String fullHashName = NAMES_TO_ALGORITHMS.get(algorithm);
            msgDigest = MessageDigest.getInstance(fullHashName);
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
