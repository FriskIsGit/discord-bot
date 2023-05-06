package bot.utilities.formatters;

public enum Token{
    //string x = "53zz"
    //int z = 23
    //NAME can be a variable/class/function/method name
    KEYWORD, NAME, ASSIGNMENT, NUMBER, STRING, CHAR, OTHER_CHARACTER,
    //symbols {} () [] ; : // >= <= < > / * + -
    CURLY_BRACKET_OPEN, CURLY_BRACKET_CLOSE, ROUND_BRACKET_OPEN, ROUND_BRACKET_CLOSE,
    SQUARE_BRACKET_OPEN, SQUARE_BRACKET_CLOSE, DOT, LAMBDA,
    SEMICOLON, COLON, COMMA, COMPARISON, OPERATOR, SHORTHAND_OPERATOR, INCREMENT, DECREMENT,
    COMMENT, ANNOTATION, DIAMOND_OPERATOR,
}
