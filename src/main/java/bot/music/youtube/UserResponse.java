package bot.music.youtube;

import java.io.File;
import java.util.Arrays;

public class UserResponse{
    public boolean success;
    public boolean hasFile = false;
    public String[] message;
    public File fileAttachment;

    private UserResponse(boolean success, String... message){
        this.success = success;
        this.message = message;
    }

    private UserResponse(File file, String... message){
        this.success = true;
        this.message = message;
        if(file != null){
            hasFile = true;
        }
        this.fileAttachment = file;
    }
    public static UserResponse fail(String... message){
        return new UserResponse(false, message);
    }
    public static UserResponse success(String... message){
        return new UserResponse(true, message);
    }
    public static UserResponse success(File file, String... message){
        return new UserResponse(file, message);
    }

    @Override
    public String toString(){
        return "UserResponse{" +
                "success=" + success +
                ", message='" + Arrays.toString(message) + '\'' +
                ", fileAttachment=" + fileAttachment +
                '}';
    }
}
