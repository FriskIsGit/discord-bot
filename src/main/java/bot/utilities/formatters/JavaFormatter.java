package bot.utilities.formatters;

import java.util.List;
import static bot.utilities.formatters.Token.*;

// EXPERIMENT
public class JavaFormatter{
    private static final String TAB = "    ";

    private TextToken last = null;
    private int indent = 0, roundOpened = 0, forBrackets = 0, switchBrackets;
    //beganFor - expression, beganCase - indentation persists until break or lambda
    private boolean beganFor = false, beganCase = false, newLine = true;
    private final StringBuilder content;
    private final char[] code;

    public JavaFormatter(char[] code){
        this.code = code;
        this.content = new StringBuilder(code.length);
    }

    private String format(){
        List<TextToken> tokens = JavaTokenizer.tokenize(code);

        for (TextToken textToken : tokens){
            switch (textToken.token){
                case KEYWORD:
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }else if (last.isNot('(')){
                        content.append(' ');
                    }
                    content.append(textToken.text());
                    if (textToken.is("for")){
                        beganFor = true;
                    }else if (textToken.is("case") || textToken.is( "default")){
                        beganCase = true;
                        indent++;
                    }else if (textToken.is("break") || textToken.is("return")){
                        if (beganCase){
                            beganCase = false;
                            indent--;
                        }
                    }
                    break;
                case NAME:
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }else if (last.isNot("[", "(", "<", "'", ":", ".", "++", "--")){
                        content.append(' ');
                    }
                    content.append(textToken.text());
                    break;
                case SEMICOLON:
                    if (beganFor){
                        content.append(";");
                    }else if(last.is(CURLY_BRACKET_CLOSE)){
                        trimLastIfNewLine();
                        content.append(";\n");
                        newLine = true;
                    }
                    else{
                        content.append(";\n");
                        newLine = true;
                    }
                    break;
                case CHAR:
                case OTHER_CHARACTER:
                    content.append(textToken.text());
                    break;
                case NUMBER:
                    if(last.isNot(SQUARE_BRACKET_OPEN) && last.isNot(ROUND_BRACKET_OPEN)){
                        content.append(' ');
                    }
                    content.append(textToken.text());
                    break;
                case CURLY_BRACKET_OPEN:
                    if (beganFor){
                        forBrackets++;
                    }
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }
                    indent++;
                    content.append("{\n");
                    newLine = true;
                    break;
                case CURLY_BRACKET_CLOSE:
                    if (beganFor){
                        beganFor = false;
                    }
                    indent--;
                    applyTab();
                    content.append("}\n");
                    newLine = true;
                    break;
                case ROUND_BRACKET_OPEN:
                    roundOpened++;
                    if (beganFor){
                        forBrackets++;
                    }
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }
                    if(last.is(KEYWORD)){
                        content.append(' ');
                    }
                    content.append('(');
                    break;
                case ROUND_BRACKET_CLOSE:
                    roundOpened--;
                    if (beganFor){
                        if (--forBrackets == 0){
                            beganFor = false;
                        }
                    }
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }
                    content.append(')');
                    break;
                case STRING:
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }
                    if(last.is("case") || last.is(ASSIGNMENT)){
                        content.append(' ');
                    }
                    content.append(textToken.text());
                    break;
                case DIAMOND_OPERATOR:
                case DOT:
                case COMMA:
                case INCREMENT:
                case DECREMENT:
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }
                    content.append(textToken.text());
                    break;
                case SQUARE_BRACKET_OPEN:
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }
                    content.append('[');
                    break;
                case SQUARE_BRACKET_CLOSE:
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }
                    content.append(']');
                    break;
                case ASSIGNMENT:
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }else{
                        content.append(' ');
                    }
                    content.append('=');
                    break;
                case ANNOTATION:
                    if (newLine){
                        applyTab();
                    }
                    newLine = true;
                    content.append(textToken.text()).append('\n');
                    break;
                case COLON:
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }else if (beganFor){
                        content.append(' ');
                    }else if (beganCase){
                        content.append(":\n");
                        newLine = true;
                        break;
                    }
                    content.append(textToken.text());
                    break;
                case LAMBDA:
                    if (beganCase){
                        beganCase = false;
                    }
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }
                    content.append(" -> ");
                    break;
                case COMMENT:
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }else{
                        content.append(' ');
                    }
                    content.append(textToken.text()).append('\n');
                    newLine = true;
                    break;
                default:
                    if (newLine){
                        applyTab();
                        newLine = false;
                    }else if (last.isNot('(')){
                        content.append(' ');
                    }
                    content.append(textToken.text());
                    break;
            }
            last = textToken;
        }
        return content.toString();
    }

    private void trimLastIfNewLine(){
        int lastIndex = content.length() - 1;
        if(content.charAt(lastIndex) == '\n'){
            content.setLength(lastIndex);
        }
    }

    public static String format(char[] code){
        return new JavaFormatter(code).format();
    }
    public static String format(String code){
        return new JavaFormatter(code.toCharArray()).format();
    }

    private void applyTab(){
        StringBuilder str = new StringBuilder(Math.max(0, TAB.length() * indent));
        for (int i = 0; i < indent; i++){
            str.append(TAB);
        }
        content.append(str);
    }

}
