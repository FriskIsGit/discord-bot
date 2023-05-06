package bot.utilities.formatters;

public class Keywords{
    public static String[] byLanguage(Language lang){
        switch (lang){
            case JAVA:
                return new String[]{
                        "abstract", "assert", "boolean", "break",
                        "byte", "case", "catch", "char",
                        "class", "const", "continue", "default",
                        "do", "double", "else", "enum",
                        "extends", "final", "finally", "float",
                        "for", "goto", "if", "int", "interface",
                        "import", "instanceof", "long", "native",
                        "new", "package", "private", "protected",
                        "public", "return", "short", "static",
                        "strictfp", "super", "switch", "synchronized",
                        "this", "throw", "throws", "transient",
                        "try", "void", "volatile", "while"
                };
            case C:
                return new String[]{
                        "auto", "break", "case", "char",
                        "const", "continue", "default", "do",
                        "double", "else", "enum", "extern",
                        "float", "for", "goto", "if",
                        "int", "long", "register", "return",
                        "short", "signed", "sizeof", "static",
                        "struct", "switch", "typedef", "union",
                        "unsigned", "void", "volatile", "while"
                };
            default:
                return null;
        }
    }

}
