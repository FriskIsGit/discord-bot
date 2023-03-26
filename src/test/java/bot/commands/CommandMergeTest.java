package bot.commands;

import bot.deskort.commands.Commands;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandMergeTest{
    @Test
    public void test1(){
        String[] given = {"aw", "er", "ty"};
        String expected = "awerty";
        assertEquals(expected, Commands.mergeTerms(given));
    }
    @Test
    public void test2(){
        String[] given = {"try"};
        String expected = "try";
        assertEquals(expected, Commands.mergeTerms(given));
    }
}
