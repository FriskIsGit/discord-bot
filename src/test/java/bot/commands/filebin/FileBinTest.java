package bot.commands.filebin;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class FileBinTest{
    private static final int CREATED = 201;
    private static final int INSUFFICIENT_STORAGE = 507;

    @Test
    public void post(){
        File file = new File("You_saw_the_ad_in_the_times.mp4");
        if(!file.exists()){
            System.err.println("File doesn't exist");
            return;
        }
        BinPostResult bin = FileBinCommand.doFileBinPost(file, "ad.mp4");
        System.out.println(bin);
        if(!bin.responseOption.isSome()){
            Assert.fail();
        }
        int code = bin.responseOption.get().code;
        assertTrue(code == CREATED || code == INSUFFICIENT_STORAGE);
    }
}
