package bot.utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileSeekerTest{
    @Test
    public void commonFileName(){
        String input = "info.txt";
        String actual = FileSeeker.getNameWithoutExtension(input);
        assertEquals("info", actual);
    }
    @Test
    public void commonFileExt(){
        String input = "info.txt";
        String actual = FileSeeker.getExtension(input);
        assertEquals("txt", actual);
    }
    @Test
    public void endingInDot(){
        String input = "something.";
        String actual = FileSeeker.getNameWithoutExtension(input);
        assertEquals("something.", actual);
    }
    @Test
    public void hiddenName(){
        //hidden file without extension
        String input = ".m2";
        String actual = FileSeeker.getNameWithoutExtension(input);
        assertEquals(".m2", actual);
    }
    @Test
    public void hiddenExt(){
        String input = ".m2";
        String actual = FileSeeker.getExtension(input);
        assertEquals("", actual);
    }
    @Test
    public void multipleDotsName(){
        String input = "DFS.2013.05.25.0300.csv";
        String actual = FileSeeker.getNameWithoutExtension(input);
        assertEquals("DFS.2013.05.25.0300", actual);
    }
    @Test
    public void multipleDotsExtension(){
        String input = "DFS.2013.05.25.0300.csv";
        String actual = FileSeeker.getExtension(input);
        assertEquals("csv", actual);
    }
}
