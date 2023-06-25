package bot.utilities.requests;

import bot.utilities.requests.Params;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParamsTest{
    @Test
    public void test1(){
        String expected = "?key=671w6&q=what";
        String params = Params.New()
                .addPair("key", "671w6")
                .addPair("q", "what")
                .get();
        assertEquals(expected, params);
    }
    @Test
    public void test2(){
        String expected = "?key=671w6&q=what&i=32&double=63.2";
        String params = Params.New()
                .addPair("key", "671w6")
                .addPair("q", "what")
                .addPair("i", 32)
                .addPair("double", 63.2)
                .get();
        assertEquals(expected, params);
    }
    @Test
    public void whitespaceTest(){
        String expected1 = "?query=this%20is%20apart";
        String expected2 = "?query=this+is+apart";
        String params = Params.New()
                .addPair("query", "this is apart")
                .get();
        assertTrue(expected1.equals(params) || expected2.equals(params));
    }
}
