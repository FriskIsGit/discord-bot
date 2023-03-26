package bot.commands;

import bot.deskort.commands.Commands;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class CommandDoubleSplitterTest{

    @Test
    public void empty(){
        String[] expected = {"", ""};
        String[] split = Commands.doubleTermSplit("");
        assertArrayEquals(expected,split);
    }
    @Test
    public void shortTrailing(){
        String[] expected = {"g", ""};
        String[] split = Commands.doubleTermSplit("g                 ");
        assertArrayEquals(expected,split);
    }
    @Test
    public void shortTest(){
        String[] expected = {"g", ""};
        String[] split = Commands.doubleTermSplit("g");
        assertArrayEquals(expected,split);
    }
    @Test
    public void test1(){
        String[] expected = {"any", "12353"};
        String[] split = Commands.doubleTermSplit("any 12353");
        assertArrayEquals(expected,split);
    }
    @Test
    public void test2(){
        String[] expected = {"large", "space"};
        String[] split = Commands.doubleTermSplit("large          space");
        assertArrayEquals(expected,split);
    }
    @Test
    public void trailing(){
        String[] expected = {"trailing", "spaces       "};
        String[] split = Commands.doubleTermSplit("trailing          spaces       ");
        assertArrayEquals(expected,split);
    }
    @Test
    public void test3(){
        String[] expected = {"extendedfirstterm", "huh"};
        String[] split = Commands.doubleTermSplit("extendedfirstterm huh");
        assertArrayEquals(expected,split);
    }
    @Test
    public void test4(){
        String[] expected = {"singleterm", ""};
        String[] split = Commands.doubleTermSplit("singleterm");
        assertArrayEquals(expected,split);
    }
    @Test
    public void test5(){
        String[] expected = {"extendedfirsttermwithspaces", ""};
        String[] split = Commands.doubleTermSplit("extendedfirsttermwithspaces           ");
        assertArrayEquals(expected,split);
    }
    @Test
    public void thirdTerm(){
        String[] expected = {"first", "second third"};
        String[] split = Commands.doubleTermSplit("first second third");
        assertArrayEquals(expected,split);
    }
    @Test
    public void singles(){
        String[] expected = {"f", "st"};
        String[] split = Commands.doubleTermSplit("f st");
        assertArrayEquals(expected,split);
    }
    @Test
    public void manyTerms(){
        String[] expected = {"join", "a channel with a space in its name"};
        String[] split = Commands.doubleTermSplit("join a channel with a space in its name");
        assertArrayEquals(expected,split);
    }
    @Test
    public void evenMoreTerms(){
        String[] expected = {"join", "46ldf gf with a space in its name"};
        String[] split = Commands.doubleTermSplit("join 46ldf gf with a space in its name");
        assertArrayEquals(expected,split);
    }
    @Test
    public void indexTest(){
        String[] expected = {"", ""};
        String[] split1 = Commands.doubleTermSplit("pop", 5);
        String[] split2 = Commands.doubleTermSplit("pop", 3);
        String[] split3 = Commands.doubleTermSplit("pop", -1);
        assertArrayEquals(expected,split1);
        assertArrayEquals(expected,split2);
        assertArrayEquals(expected,split3);
    }
}
