package bot.utilities.formatters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static bot.utilities.formatters.Language.JAVA;
import static bot.utilities.formatters.Token.*;

public class JavaTokenizer{
    private static final String[] KEYWORDS = Keywords.byLanguage(JAVA);

    private final List<TextToken> tokens = new ArrayList<>();
    private final char[] arr;
    private final int len;
    private int i = 0;
    private int compareSymbols = 0;

    private JavaTokenizer(char[] arr){
        this.arr = arr;
        this.len = arr.length;
    }
    public static List<TextToken> tokenize(char[] code){
        return new JavaTokenizer(code).tokenize();
    }

    private List<TextToken> tokenize(){
        for (i = 0; i < len; i++){
            char chr = arr[i];
            boolean hasNext = i+1 < len;
            switch (chr){
                case '"':
                    int quote = nextQuote();
                    String str = subArray(i, quote + 1);
                    tokens.add(TextToken.as(STRING, escapeSpecialCharacters(str)));
                    i = quote;
                    break;
                case '{':
                    tokens.add(TextToken.as(CURLY_BRACKET_OPEN, '{'));
                    break;
                case '}':
                    tokens.add(TextToken.as(CURLY_BRACKET_CLOSE, '}'));
                    break;
                case '[':
                    tokens.add(TextToken.as(SQUARE_BRACKET_OPEN, '['));
                    break;
                case ']':
                    tokens.add(TextToken.as(SQUARE_BRACKET_CLOSE, ']'));
                    break;
                case '(':
                    tokens.add(TextToken.as(ROUND_BRACKET_OPEN, '('));
                    break;
                case ')':
                    tokens.add(TextToken.as(ROUND_BRACKET_CLOSE, ')'));
                    break;
                case '=':
                    if(hasNext && arr[i+1] == '='){
                        tokens.add(TextToken.as(COMPARISON, "=="));
                        i++;
                        break;
                    }
                    tokens.add(TextToken.as(ASSIGNMENT, '='));
                    break;
                case ';':
                    tokens.add(TextToken.as(SEMICOLON, ';'));
                    break;
                case ':':
                    tokens.add(TextToken.as(COLON, ':'));
                    break;
                case '@':
                    int annotationEnd = annotationEnd();
                    tokens.add(TextToken.as(COLON, subArray(i, annotationEnd)));
                    break;
                case ',':
                    tokens.add(TextToken.as(COMMA, ','));
                    break;
                case '\'':
                    int chrEnd = charEnd();
                    if(chrEnd == -1){
                        tokens.add(TextToken.as(OTHER_CHARACTER, chr));
                        break;
                    }
                    System.out.println("SUB ARRAY OF i, e: " + i + ", " + chrEnd);
                    String charToken = subArray(i, chrEnd);
                    tokens.add(TextToken.as(CHAR, escapeSpecialCharacters(charToken)));
                    i = chrEnd;
                    break;
                case '!':
                    if(hasNext && arr[i+1] == '='){
                        tokens.add(TextToken.as(COMPARISON, "!="));
                        i++;
                        break;
                    }
                    tokens.add(TextToken.as(OPERATOR, chr));
                    break;
                case '<':
                    if (hasNext){
                        if(arr[i+1] == '='){
                            tokens.add(TextToken.as(COMPARISON, "<="));
                            i++;
                            break;
                        }else if(arr[i+1] == '>'){
                            tokens.add(TextToken.as(DIAMOND_OPERATOR, "<>"));
                            i++;
                            break;
                        }else if(arr[i+1] == '<'){
                            if(i+2 < len && arr[i+2] == '='){
                                tokens.add(TextToken.as(SHORTHAND_OPERATOR, "<<="));
                                i+=2;
                                break;
                            }
                            tokens.add(TextToken.as(OPERATOR, "<<"));
                            i++;
                            break;
                        }
                    }
                    compareSymbols++;
                    tokens.add(TextToken.as(COMPARISON, chr));
                    break;
                case '>':
                    if (hasNext){
                        if(arr[i + 1] == '='){
                            tokens.add(TextToken.as(COMPARISON, ">="));
                            i++;
                            break;
                        }else if(arr[i+1] == '>'){
                            if(i+2 < len && arr[i+2] == '='){
                                tokens.add(TextToken.as(SHORTHAND_OPERATOR, ">>="));
                                i += 2;
                                break;
                            }
                            tokens.add(TextToken.as(OPERATOR, ">>"));
                            i++;
                            break;
                        }
                    }
                    if(--compareSymbols == 0){
                        tokens.add(TextToken.as(COMPARISON, '>'));
                        resolveForPossibleDiamondOperator();
                        break;
                    }
                    tokens.add(TextToken.as(COMPARISON, chr));
                    break;
                case ' ':
                case '\n':
                    //ignore?
                    break;
                case '/':
                    if (hasNext){
                        if(arr[i+1] == '/'){
                            int end = indexOf('\n');
                            tokens.add(TextToken.as(COMMENT, subArray(i, end)));
                            i = end;
                            break;
                        }else if(arr[i+1] == '='){
                            tokens.add(TextToken.as(SHORTHAND_OPERATOR, "/="));
                            i++;
                            break;
                        }else if(arr[i+1] == '*'){
                            int end = docCommentEnd();
                            tokens.add(TextToken.as(COMMENT, subArray(i, end)));
                            i = end - 1;
                            break;
                        }
                    }
                    tokens.add(TextToken.as(OPERATOR, '/'));
                    break;
                case '+':
                    if (hasNext){
                        if(arr[i+1] == '+'){
                            tokens.add(TextToken.as(INCREMENT, "++"));
                            i++;
                            break;
                        }else if(arr[i+1] == '='){
                            tokens.add(TextToken.as(SHORTHAND_OPERATOR, "+="));
                            i++;
                            break;
                        }
                    }
                    tokens.add(TextToken.as(OPERATOR, chr));
                    break;
                case '-':
                    if (hasNext){
                         if(arr[i+1] == '-'){
                             tokens.add(TextToken.as(DECREMENT, "--"));
                             i++;
                             break;
                         }else if(arr[i+1] == '>'){
                             tokens.add(TextToken.as(LAMBDA, "->"));
                             i++;
                             break;
                         }else if(arr[i+1] == '='){
                             tokens.add(TextToken.as(SHORTHAND_OPERATOR, "-="));
                             i++;
                             break;
                         }
                    }
                    tokens.add(TextToken.as(OPERATOR, chr));
                    break;
                //check for shorthands
                case '*':
                case '%':
                case '&':
                case '|':
                case '^':
                    if(hasNext && arr[i+1] == '='){
                        tokens.add(TextToken.as(SHORTHAND_OPERATOR, "" + chr + '='));
                        i++;
                        break;
                    }
                    tokens.add(TextToken.as(OPERATOR, chr));
                    break;
                case '~':
                    tokens.add(TextToken.as(OPERATOR, chr));
                    break;
                case '.':
                    tokens.add(TextToken.as(DOT, chr));
                    break;
                default:
                    //keyword, name, number, unknown char,
                    if(Character.isLetter(chr)){
                        //name or keyword
                        int end = nextNameDelimiter();
                        String name = subArray(i, end);
                        if(isKeyword(name)){
                            tokens.add(TextToken.as(KEYWORD, name));
                        }else{
                            tokens.add(TextToken.as(NAME, name));
                        }
                        i = end - 1;
                    }else if(chr == '$' || chr == '_'){
                        //definitely a name
                        int end = nextNameDelimiter();
                        tokens.add(TextToken.as(NAME, subArray(i, end)));
                        i = end - 1;
                    }else if (Character.isDigit(chr)){
                        int end = nextNumberDelimiter();
                        tokens.add(TextToken.as(NUMBER, subArray(i, end)));
                        i = end - 1;
                    }else{
                        tokens.add(TextToken.as(OTHER_CHARACTER, chr));
                        System.err.println("Raised exception for char: " + chr + " at index: " + i);
                        break;
                    }
            }
        }
        return tokens;
    }

    //returns exclusive char end index or -1 if not a char
    private int charEnd(){
        // '\n' or '\''- max 2 characters inside
        if(i + 1 < len){
            if(arr[i+1] == '\\'){
                if(i + 3 < len && arr[i+3] == '\''){
                    return i + 4;
                }else{
                    return -1;
                }
            }else{
                if(i + 2 < len && arr[i+2] == '\''){
                    return i + 3;
                }else{
                    return -1;
                }
            }
        }else {
            return -1;
        }
    }

    //returns exclusive index
    private int annotationEnd(){
        int j = i+1;
        int roundBracketsOpen = 0;
        boolean inString = false;
        while (j < len){
            char chr = arr[j];
            switch (chr){
                case '$':
                case '_':
                    j++;
                    break;
                case '"':
                    inString = !inString;
                    j++;
                    break;
                case '(':
                    roundBracketsOpen++;
                    j++;
                    break;
                case ')':
                    if(--roundBracketsOpen == 0){
                        return j + 1;
                    }
                    break;
                case ' ':
                case '\n':
                    return j;
                default:
                    if(Character.isLetter(chr) || Character.isDigit(chr)){
                        j++;
                        break;
                    }
                    else{
                        return j;
                    }
            }
        }
        return j;
    }

    //called on valid closed '>' to decide if token types should be changed
    private void resolveForPossibleDiamondOperator(){
        int count = 0;
        int lastIndex = tokens.size()-1;
        List<TextToken> toModify = new ArrayList<>(4);
        //keywords, names, commas, other diamond operators
        outer:
        for (int j = lastIndex; j > -1; j--){
            TextToken txtToken = tokens.get(j);
            switch (txtToken.token){
                case KEYWORD:
                case NAME:
                case COMMA:
                case DIAMOND_OPERATOR:
                    break;
                case COMPARISON:
                    if(txtToken.is('>')){
                        toModify.add(txtToken);
                        count++;
                    }else if(txtToken.is('<')){
                        toModify.add(txtToken);
                        if(--count == 0){
                            for(TextToken tt : toModify){
                                tt.token = DIAMOND_OPERATOR;
                            }
                            return;
                        }
                    }
                    else{
                        return;
                    }
                    break;
                default:
                    System.err.println("QUIT ON DEFAULt");
                    System.out.println(txtToken);
                    //don't do anything as it's not a diamond operator
                    return;
            }
        }
        System.err.println("Greatly failed at changing tokens");
    }

    //lack of this would ruin the purpose of formatting if a string can contain an unescaped new line character
    private static String escapeSpecialCharacters(String str){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            switch (c){
                case '\n':
                    builder.append("\\n");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                default:
                    builder.append(c);
                    break;
            }
        }
        return builder.toString();
    }

    private boolean isEscapedCharacter(char chr){
        switch (chr){
            case 't':
            case 'b':
            case 'n':
            case 'r':
            case 'f':
            case 's':
            case '\'':
            case '"':
            case '\\':
                return true;
            default:
                return false;
        }
    }

    private int indexOf(char chr, int from){
        for (int i = from; i < len; i++){
            if(arr[i] == chr)
                return i;
        }
        return -1;
    }

    private int nextNameDelimiter(){
        int j = i+1;
        while (j < len){
            char chr = arr[j];
            if(Character.isLetter(chr) || Character.isDigit(chr) || chr == '$' || chr == '_'){
                j++;
            }else{
                return j;
            }
        }
        return j;
    }
    //returns exclusive index
    private int nextNumberDelimiter(){
        int j = i+1;
        while (j < len){
            char chr = arr[j];
            switch (chr){
                case '_':
                case '.':
                    j++;
                    continue;
                case 'D':
                case 'd':
                case 'f':
                case 'F':
                case 'l':
                case 'L':
                    return j + 1;
                case ' ':
                case ';':
                    return j;
            }
            if(Character.isDigit(chr)){
                j++;
            }else{
                return j;
            }
        }
        return j + 1;
    }

    //returns specified chr index (inclusive) or exclusive index if EOF
    private int indexOf(char chr){
        int j = i+1;
        while (j < len){
            if(arr[j] == chr){
                return j;
            }
            j++;
        }
        return j;
    }

    //returns exclusive index
    private int docCommentEnd(){
        int j = i+1;
        while (j < len-1){
            if(arr[j] == '*' && arr[j+1] == '/'){
                return j+2;
            }
            j++;
        }
        return len;
    }

    //reduced by one index if EOF
    private int nextQuote(){
        int j = i+1;
        while (j < len){
            if(arr[j] == '"'){
                return j;
            }
            j++;
        }
        return j - 1;
    }

    private static boolean isKeyword(String str){
        return Arrays.binarySearch(KEYWORDS, str) >= 0;
    }

    //from - inclusive, to - exclusive
    private String subArray(int from, int to){
        int count = to - from;
        return new String(arr, from, count);
    }

}
