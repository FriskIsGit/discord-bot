package bot.commands.lyrics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LyricsCommandTest {
    @Test
    public void misspelled() {
        String coat = "coat";
        String cost = "cost";
        assertEquals(3, LyricsCommand.matchingLen(coat, cost));
    }

    @Test
    public void equality() {
        assertEquals(4, LyricsCommand.matchingLen("coat", "coat"));
    }

    @Test
    public void missingLetter() {
        assertEquals(3, LyricsCommand.matchingLen("cot", "coat"));
    }

    @Test
    public void additionalLetter() {
        assertEquals(3, LyricsCommand.matchingLen("coat", "cot"));
    }

    @Test
    public void littleToNoMatching() {
        String str1 = "recur";
        String str2 = "owner";
        assertEquals(1, LyricsCommand.matchingLen(str1, str2));
    }

    @Test
    public void noMatching() {
        assertEquals(0, LyricsCommand.matchingLen("0", "something"));
    }

    @Test
    public void test() {
        assertEquals(5, LyricsCommand.matchingLen("lyrics", "lyics"));
    }

    @Test
    public void lilTest1() {
        assertEquals(0.6f, LyricsCommand.accuracy("lil yachty", "lil yachty poland"), 0.0f);
    }

    @Test
    public void lilTest2() {
        assertEquals(0.6f, LyricsCommand.accuracy("lil yachty", "lil yachty poland"), 0.1f);
    }

    @Test
    public void lilTest3() {
        assertEquals(0.25, LyricsCommand.accuracy("lil yachty poland", "Genius Brasil Traduções - Lil Yachty - Poland (Tradução em Português)"), 0.1f);
    }
}
