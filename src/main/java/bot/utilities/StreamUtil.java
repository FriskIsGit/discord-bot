package bot.utilities;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtil{
    public static String streamToString(InputStream inputStream){
        return streamToString(inputStream, 32768);
    }

    public static String streamToString(InputStream inputStream, int initialSize){
        String output;
        byte[] buffer = new byte[initialSize];
        try {
            int offset = 0;
            while (inputStream.available() != 0) {
                int available = inputStream.available();
                if(available + offset < buffer.length){
                    int currentRead = inputStream.read(buffer, offset, available);
                    offset += currentRead;
                }else{
                    byte[] tempBuffer = new byte[buffer.length<<1];
                    System.arraycopy(buffer, 0, tempBuffer, 0, offset);
                    buffer = tempBuffer;
                    tempBuffer = null;
                }
            }
            output = bytesToStr(buffer, offset);
            return output;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("Stream closed?");
        }
        return null;
    }

    private static String bytesToStr(byte[] bytes, int offset){
        char[] charArr = new char[offset];
        for(int i = 0; i<offset; i++){
            charArr[i] = (char)bytes[i];
        }
        return new String(charArr);
    }
}
