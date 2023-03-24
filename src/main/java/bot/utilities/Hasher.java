package bot.utilities;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static String hashBytes(byte[] bytes, Hash hash){
        byte[] hashedBytes = hash.msgDigest.digest(bytes);
        StringBuilder hexString = new StringBuilder(hash.length);
        for (byte b : hashedBytes){
            String symbol = Integer.toString((b & 0xff) + 0x100, 16);
            hexString.append(symbol, 1, symbol.length());
        }
        return hexString.toString();
    }
    public static String hash(String text, Hash hash){
        return hashBytes(text.getBytes(StandardCharsets.UTF_8), hash);
    }
    public static Hash choose(String name){
        switch (name.toLowerCase()){
            case "sha224":
                return Hasher.SHA_224;
            case "hash":
            case "sha256":
                return Hasher.SHA_256;
            case "sha384":
                return Hasher.SHA_384;
            case "sha512":
                return Hasher.SHA_512;
            case "sha1":
                return Hasher.SHA_1;
            case "md5":
                return Hasher.MD5;
            default:
                throw new IllegalStateException("No match for given algorithm");
        }
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