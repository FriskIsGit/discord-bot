package bot.utilities;

public class NotNull{
    //for definitely not null use cases
    public static <T> T notNull(T obj){
        if(obj == null){
            throw new NullPointerException("Unexpected null");
        }
        return obj;
    }
}
