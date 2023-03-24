package bot.music.youtube;

import java.io.File;

public class UserResponse{
    public boolean success;
    public boolean hasFile = false;
    public String message;
    public File fileAttachment;

    private UserResponse(String message, boolean success){
        this.success = success;
        this.message = message;
    }

    private UserResponse(String message, File file){
        this.success = true;
        this.message = message;
        if(file != null){
            hasFile = true;
        }
        this.fileAttachment = file;
    }
    public static UserResponse fail(String message){
        return new UserResponse(message, false);
    }
    public static UserResponse success(String message){
        return new UserResponse(message, true);
    }
    public static UserResponse success(String message, File file){
        return new UserResponse(message, file);
    }

    @Override
    public String toString(){
        return "UserResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", fileAttachment=" + fileAttachment +
                '}';
    }
}
