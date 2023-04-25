package bot.commands;

import org.junit.Test;
import bot.deskort.commands.Commands;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class CommandAnySplitterTest{

    @Test
    public void test1(){
        String[] expected = {"q", "w", "e"};
        String[] split = Commands.splitIntoTerms("    q    w      e       ",0);
        assertArrayEquals(expected, split);
    }
    @Test
    public void empty(){
        String[] expected = {""};
        String[] split = Commands.splitIntoTerms("",0);
        assertArrayEquals(expected,split);
    }
    @Test
    public void test2(){
        String[] expected = {"qwerty", "123", "desuk"};
        String[] split = Commands.splitIntoTerms("qwerty  123 desuk",0);
        assertArrayEquals(expected, split);
    }
    @Test
    public void test3(){
        String[] expected = {"we", "are", "the", "people"};
        String[] split = Commands.splitIntoTerms("  we  are the people",0);
        assertArrayEquals(expected, split);
    }
    @Test
    public void test4(){
        String[] expected = {"z", "qa"};
        String[] split = Commands.splitIntoTerms("  z qa",0);
        assertArrayEquals(expected, split);
    }
    @Test
    public void test5(){
        String[] expected = {"quasi", "9", "8", "76"};
        String[] split = Commands.splitIntoTerms("quasi 9  8   76 ",0);
        assertArrayEquals(expected, split);
    }
    @Test
    public void singleTerm(){
        String[] expected = {"zzz"};
        String[] split = Commands.splitIntoTerms("zzz",0);
        assertArrayEquals(expected, split);
    }
    @Test
    public void singleTermFromIndexTest1(){
        String[] expected = {"zz"};
        String[] split = Commands.splitIntoTerms("zzz",1);
        assertArrayEquals(expected,split);
    }
    @Test
    public void FromIndexTest2(){
        String[] expected = {"ytvideo", "34j_DKjqWe", "2"};
        String[] split = Commands.splitIntoTerms(">.-=ytvideo 34j_DKjqWe  2",4);
        assertArrayEquals(expected,split);
    }
    @Test
    public void FromIndexTest3(){
        String[] expected = {"ytinfo", "id", "num"};
        String[] split = Commands.splitIntoTerms("=ytinfo id num",1);
        assertArrayEquals(expected,split);
    }
    @Test
    public void FromIndexTest4(){
        String[] expected = {"ytinfo", "id", "num"};
        String[] split = Commands.splitIntoTerms(" =ytinfo id num",2);
        assertArrayEquals(expected,split);
    }
    @Test
    public void threeInRowTest1(){
        String[] expected = {"ytinfo", "link", "2"};
        String[] split = Commands.splitIntoTerms("ytinfo link 2",0);
        assertArrayEquals(expected,split);
    }
    @Test
    public void threeInRowTest2(){
        String[] expected = {"yt", "link", "tw"};
        String[] split = Commands.splitIntoTerms("yt link tw",0);
        assertArrayEquals(expected,split);
    }
    @Test
    public void anySplitTrailing(){
        String[] expected = {"au", "somethong"};
        String[] split = Commands.splitIntoTerms("au  somethong  ",0);
        assertArrayEquals(expected,split);
    }
    @Test
    public void anySplitTrailingThree(){
        String[] expected = {"audit", "some", "23"};
        String[] split = Commands.splitIntoTerms("audit  some  23 ",0);
        assertArrayEquals(expected,split);
    }
    @Test
    public void indexTest(){
        String[] expected = {""};
        String[] split1 = Commands.splitIntoTerms("pop", 5);
        String[] split2 = Commands.splitIntoTerms("pop", 3);
        String[] split3 = Commands.splitIntoTerms("pop", -1);
        assertArrayEquals(expected,split1);
        assertArrayEquals(expected,split2);
        assertArrayEquals(expected,split3);
    }
    @Test
    public void quotesTest1(){
        String input = "wait \"what  would\" that be";
        String[] expected = {"wait", "what  would", "that", "be"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void twoQuotesOpenedAtATime(){
        String input = "wait \"\"what  would\"\" that be";
        String[] expected = {"wait", "\"\"what", "would\"\"", "that", "be"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void unclosedQuoteTrailing(){
        String input = "wait \"what  ";
        String[] expected = {"wait", "\"what"};
        String[] output = Commands.splitIntoTerms(input, 0);
        printArr(expected);
        printArr(output);
        assertArrayEquals(expected, output);
    }
    @Test
    public void unclosedQuote(){
        String input = "wait \"what";
        String[] expected = {"wait", "\"what"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void unclosedQuotesTrailingTerm(){
        String input = "wait \"what  term";
        String[] expected = {"wait", "\"what", "term"};
        String[] output = Commands.splitIntoTerms(input, 0);
        printArr(expected);
        printArr(output);
        assertArrayEquals(expected, output);
    }
    @Test
    public void quoteGlued(){
        String input = "glue\"plastic  last";
        String[] expected = {"glue\"plastic", "last"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void twoQuotesGlued(){
        String input = "glue\"plastic\"end  last";
        String[] expected = {"glue\"plastic\"end", "last"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void extractedFromQuotes(){
        String input = "\"quote\" last";
        String[] expected = {"quote", "last"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void notQuotes(){
        String input = "\"quote\"last";
        String[] expected = {"\"quote\"last"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void emptyQuotes1(){
        String input = "\"\"last";
        String[] expected = {"\"\"last"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void emptyQuotes2(){
        String input = "\"\" last";
        String[] expected = {"\"\"","last"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void multipleNeverClosed(){
        String input = "\"  x a z last";
        String[] expected = {"\"", "x", "a", "z", "last"};
        String[] output = Commands.splitIntoTerms(input, 0);
        System.out.println(expected.length);
        System.out.println(output.length);
        assertArrayEquals(expected, output);
    }
    @Test
    public void whitespacesInQuotes2(){
        String input = "\"x a z\"last";
        String[] expected = {"\"x", "a", "z\"last"};
        String[] output = Commands.splitIntoTerms(input, 0);
        printArr(expected);
        printArr(output);
        assertArrayEquals(expected, output);
    }
    @Test
    public void neverClosedQuotes(){
        String input = "\"xaz last";
        String[] expected = {"\"xaz", "last"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void oneQuoteTerm(){
        String input = "\"term\"";
        String[] expected = {"term"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void nothingInQuotes(){
        String input = "\"              \"";
        String[] expected = {"              "};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void spaceTerm(){
        String input = "dad \"  smh  \"";
        String[] expected = {"dad", "  smh  "};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void loneQuote(){
        String input = "dad \" ";
        String[] expected = {"dad", "\""};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void manyTerms(){
        String input = "command   op 123 \"M U L T I T E R M S\"";
        String[] expected = {"command", "op", "123", "M U L T I T E R M S"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void leadingSpaceManyTerms(){
        String input = "command   op 123 \" T E R M S\"";
        String[] expected = {"command", "op", "123", " T E R M S"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void leadingSpaceAndTrailingManyTerms(){
        String input = "command   op 123 \" T E R M S \"";
        String[] expected = {"command", "op", "123", " T E R M S "};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void manyQuotes(){
        String input = "command   \" T E R M S \" cute \" T E R M S \"";
        String[] expected = {"command", " T E R M S ", "cute", " T E R M S "};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void fakeQuotes1(){
        String input = "\"quote\"what";
        String[] expected = {"\"quote\"what"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void quote(){
        String input = "\"quote\" ";
        String[] expected = {"quote"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void youSure1(){
        String input = "\"q\" \"wtv\"";
        String[] expected = {"q", "wtv"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void youSure2(){
        String input = "\"op\" \" \"hey\"";
        String[] expected = {"op", "\"", "hey"};
        String[] output = Commands.splitIntoTerms(input, 0);
        printArr(expected);
        printArr(output);
        assertArrayEquals(expected, output);
    }
    @Test
    public void youSure2_5(){
        String input = "\" \"hey\"";
        String[] expected = {"\"", "hey"};
        String[] output = Commands.splitIntoTerms(input, 0);
        printArr(expected);
        printArr(output);
        assertArrayEquals(expected, output);
    }
    @Test
    public void youSure3(){
        String input = "1 \" 2def";
        String[] expected = {"1", "\"", "2def"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void youSure4(){
        String input = "\"1\" \" \"2\"";
        String[] expected = {"1", "\"", "2"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void youSure5(){
        String input = "\"1\" \" 2";
        String[] expected = {"1", "\"", "2"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void mergedQuotes1(){
        String input = "\"1st\"\"2nd";
        String[] expected = {"\"1st\"\"2nd"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void mergedQuotes2FromIndex(){
        String input = "until o\"z\"w";
        String[] expected = {"ntil", "o\"z\"w"};
        String[] output = Commands.splitIntoTerms(input, 1);
        assertArrayEquals(expected, output);
    }
    @Test
    public void codeFormat(){
        String input = "-for ja \"#include \"Enter int2\"";
        String[] expected = {"-for", "ja", "\"#include", "Enter int2"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void graveAccent1(){
        String input = "st 1 `i see \"string\"`";
        String[] expected = {"st", "1", "i see \"string\""};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void graveAsPartOfAnArgument(){
        String input = "token`grave \"`";
        String[] expected = {"token`grave", "\"`"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void closingGraveHasRules(){
        String input = "token `two `three";
        String[] expected = {"token", "`two", "`three"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void validClosingGrave(){
        String input = "to `open ` close";
        String[] expected = {"to", "open ", "close"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void graveInQuotes(){
        String input = "to \"`in quotes ` close\"";
        String[] expected = {"to", "`in quotes ` close"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void quotesInGrave(){
        String input = "then `He said \"Ok.. we gon see\".`";
        String[] expected = {"then", "He said \"Ok.. we gon see\"."};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void standAloneQuote(){
        String input = "to \" unclosed ";
        String[] expected = {"to", "\"", "unclosed"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void unclosedQuoteAndGrave(){
        String input = "to \"neverClosed `hey mate`";
        String[] expected = {"to", "\"neverClosed", "hey mate"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void complexInput(){
        String input = "ab \"cd? `ef \"gh `";
        String[] expected = {"ab", "\"cd?", "ef \"gh "};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }
    @Test
    public void leftOutGrave(){
        String input = "ab false`open `";
        String[] expected = {"ab", "false`open", "`"};
        String[] output = Commands.splitIntoTerms(input, 0);
        assertArrayEquals(expected, output);
    }

    private static void printArr(String[] arr){
        System.out.println(Arrays.toString(arr));
    }

}
