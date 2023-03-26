package bot.commands;

import bot.deskort.commands.Commands;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class CommandShrinkArrayTest{
    @Test
    public void test1(){
        String[] input = {"x", "y", "z"};
        String[] expected = {"y", "z"};
        assertArrayEquals(expected, Commands.shrink(input, 1));
    }
    @Test
    public void zeroIndexFilled(){
        String[] input = {"a", "b", "c"};
        String[] expected = {"a", "b", "c"};
        assertArrayEquals(expected, Commands.shrink(input, 0));
    }
    @Test
    public void zeroIndexEmpty(){
        String[] input = {};
        String[] expected = {};
        assertArrayEquals(expected, Commands.shrink(input, 0));
    }
    @Test
    public void negativeIndexEmpty(){
        String[] input = {};
        String[] expected = {};
        assertArrayEquals(expected, Commands.shrink(input, -1));
    }
    @Test
    public void negativeIndexFilled(){
        String[] input = {"3", "duck", "greek"};
        String[] expected = {};
        assertArrayEquals(expected, Commands.shrink(input, -1));
    }
    @Test
    public void indexTooFar(){
        String[] input = {"zed", "alpha", "bet", "print"};
        String[] expected = {};
        assertArrayEquals(expected, Commands.shrink(input, 4));
        assertArrayEquals(expected, Commands.shrink(input, 5));
        assertArrayEquals(expected, Commands.shrink(input, 6));
    }
    @Test
    public void lastElement(){
        String[] input = {"zed", "alpha", "bet", "print"};
        String[] expected = {"print"};
        assertArrayEquals(expected, Commands.shrink(input, 3));
    }
}
