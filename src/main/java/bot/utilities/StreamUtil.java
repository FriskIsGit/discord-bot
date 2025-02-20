package bot.utilities;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtil{
    public static String streamToString(InputStream inputStream){
        if(inputStream == null){
            return "";
        }
        byte[] buffer = new byte[32768];
        try {
            int writeOffset = 0;
            while (inputStream.available() != 0) {
                int available = inputStream.available();
                if(available + writeOffset < buffer.length){
                    int currentRead = inputStream.read(buffer, writeOffset, available);
                    writeOffset += currentRead;
                }else{
                    byte[] tempBuffer = new byte[buffer.length<<1];
                    System.arraycopy(buffer, 0, tempBuffer, 0, writeOffset);
                    buffer = tempBuffer;
                    tempBuffer = null;
                }
            }
            return new String(buffer, 0, writeOffset);
        } catch (IOException ignored) {}
        return null;
    }
}
