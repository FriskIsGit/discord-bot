package bot.utilities;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Hasher{
    public final static Hash SHA_512;
    public final static Hash SHA_384;
    public final static Hash SHA_256;
    public final static Hash SHA_224;
    public final static Hash SHA_1;
    public final static Hash MD5;

    static{
        try{
            SHA_512 = new Hash(MessageDigest.getInstance("SHA-512"), 128);
            SHA_384 = new Hash(MessageDigest.getInstance("SHA-384"), 96);
            SHA_256 = new Hash(MessageDigest.getInstance("SHA-256"), 64);
            SHA_224 = new Hash(MessageDigest.getInstance("SHA-224"), 56);
            SHA_1   = new Hash(MessageDigest.getInstance("SHA-1"), 40);
            MD5     = new Hash(MessageDigest.getInstance("MD5"), 32);
        }catch (NoSuchAlgorithmException noSuchAlgExc){
            throw new RuntimeException(noSuchAlgExc);
        }
    }

    public final static HashMap<String, Hash> NAMES_TO_ALGORITHMS = new HashMap<String, Hash>() {{
        put("sha224", SHA_224);
        put("hash",   SHA_256);
        put("sha256", SHA_256);
        put("sha384", SHA_384);
        put("sha512", SHA_512);

        put("sha1",   SHA_1);
        put("md5",    MD5);
    }};

    public static boolean hasAlgorithm(String alg){
        if(alg.length() > 6){
            return false;
        }
        return NAMES_TO_ALGORITHMS.containsKey(alg);
    }
    public static String hashBytes(byte[] bytes, Hash hash){
        byte[] hashedBytes = hash.msgDigest.digest(bytes);
        StringBuilder hexString = new StringBuilder(hash.length);
        for (byte b : hashedBytes){
            String symbol = Integer.toString((b & 0xff) + 0x100, 16);
            hexString.append(symbol, 1, symbol.length());
        }
        return hexString.toString();
    }
    public static String hash(String text, String name){
        return hashBytes(text.getBytes(StandardCharsets.UTF_8), NAMES_TO_ALGORITHMS.get(name));
    }

}
//serves as a wrapper, only used by this class
class Hash{
    MessageDigest msgDigest;
    int length;

    public Hash(MessageDigest msgDigest, int length){
        this.msgDigest = msgDigest;
        this.length = length;
    }
}