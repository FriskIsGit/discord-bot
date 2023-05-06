package bot.utilities.formatters;

public class TextToken{
    public Token token;
    private final String text;

    public TextToken(Token token, String text){
        this.token = token;
        this.text = text;
    }
    public TextToken(Token token, char text){
        this.token = token;
        this.text = String.valueOf(text);
    }
    public static TextToken as(Token token, char text){
        return new TextToken(token, text);
    }
    public static TextToken as(Token token, String text){
        return new TextToken(token, text);
    }

    public boolean is(Token token){
        return this.token == token;
    }
    public boolean isNot(Token token){
        return this.token != token;
    }
    public boolean isNot(char chr){
        return text.charAt(0) != chr || text.length() != 1;
    }
    public boolean isNot(String... texts){
        for (String s : texts){
            if (text.equals(s)){
                return false;
            }
        }
        return true;
    }
    public boolean is(String text){
        return this.text.equals(text);
    }
    public boolean is(char chr){
        return text.length() == 1 && text.charAt(0) == chr;
    }

    @Override
    public String toString(){
        return text;
    }

    public String text(){
        return text;
    }
}
