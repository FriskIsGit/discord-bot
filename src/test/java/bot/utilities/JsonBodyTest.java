package bot.utilities;

import bot.utilities.requests.JsonBody;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class JsonBodyTest{
    @Test
    public void stringValue(){
        String actual = JsonBody.body()
                .addPair("prompt", "This is the data")
                .get();
        String expected = "{\"prompt\":\"This is the data\"}";
        assertEquals(expected, actual);
    }
    @Test
    public void intValue(){
        String actual = JsonBody.body()
                .addPair("tokens", 531)
                .get();
        String expected = "{\"tokens\":531}";
        assertEquals(expected, actual);
    }
    @Test
    public void booleanValue(){
        String actual = JsonBody.body()
                .addPair("isAllowed", true)
                .get();
        String expected = "{\"isAllowed\":true}";
        assertEquals(expected, actual);
    }
    @Test
    public void floatValue(){
        String actual = JsonBody.body()
                .addPair("scale", 0.7f)
                .get();
        String expected = "{\"scale\":0.7}";
        assertEquals(expected, actual);
    }
    @Test
    public void doubleValue(){
        String actual = JsonBody.body()
                .addPair("factor", 8.9353)
                .get();
        String expected = "{\"factor\":8.9353}";
        assertEquals(expected, actual);
    }
    @Test
    public void longValue(){
        String actual = JsonBody.body()
                .addPair("long_id", 53193501807301709L)
                .get();
        String expected = "{\"long_id\":53193501807301709}";
        assertEquals(expected, actual);
    }
    @Test
    public void listValue(){
        String actual = JsonBody.body()
                .addPair("scale", Arrays.asList("fast", "formal", "optimized"))
                .get();
        String expected = "{\"scale\":[\"fast\",\"formal\",\"optimized\"]}";
        assertEquals(expected, actual);
    }
    @Test
    public void jsonBodyValue(){
        JsonBody nested = JsonBody.body()
                .addPair("data", "sensitive")
                .addPair("year_released", 2012);

        String actual = JsonBody.body()
                .addPair("open", true)
                .addPair("nested", nested)
                .addPair("scale", Arrays.asList("fast", "formal", "optimized"))
                .get();
        String expected =
                "{" + "\"open\":true," +
                "\"nested\":{\"data\":\"sensitive\",\"year_released\":2012}," +
                "\"scale\":[\"fast\",\"formal\",\"optimized\"]" +
                "}";
        assertEquals(expected, actual);
    }
    @Test
    public void manyValues1(){
        String actual = JsonBody.body()
                .addPair("multiplier", 1.5)
                .addPair("types", Arrays.asList("first", "2", "token"))
                .addPair("delimiter", ".")
                .get();
        String expected =
                "{" + "\"multiplier\":1.5," +
                "\"types\":[\"first\",\"2\",\"token\"]," +
                "\"delimiter\":\".\"" + "}";
        assertEquals(expected, actual);
    }
    @Test
    public void escapeList(){
        String actual = JsonBody.body()
                .addPair("paths", Arrays.asList("dev/block", "c\\users\\data", "\"d:/well\""))
                .get();
        String expected =
                "{" +
                "\"paths\":[\"dev/block\",\"c\\\\users\\\\data\",\"\\\"d:/well\\\"\"]}";
        assertEquals(expected, actual);
    }
    @Test
    public void manyValues2(){
        String actual = JsonBody.body()
                .addPair("temperature", 21)
                .addPair("ratio", 0.3f)
                .addPair("text", "Hello world!")
                .get();
        String expected = "{\"temperature\":21,\"ratio\":0.3,\"text\":\"Hello world!\"}";
        assertEquals(expected, actual);
    }
    @Test
    public void escapeQuotes(){
        String actual = JsonBody.body()
                .addPair("txt", "He said \"Okay miss.\"")
                .get();
        String expected = "{\"txt\":\"He said \\\"Okay miss.\\\"\"}";
        System.out.println(expected);
        System.out.println(actual);
        assertEquals(expected, actual);
    }
    @Test
    public void escapeReverseSlash(){
        String actual = JsonBody.body()
                .addPair("path", "home\\user\\pictures")
                .get();
        String expected = "{\"path\":\"home\\\\user\\\\pictures\"}";
        System.out.println(expected);
        System.out.println(actual);
        assertEquals(expected, actual);
    }
}
