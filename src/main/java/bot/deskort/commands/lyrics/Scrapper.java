package bot.deskort.commands.lyrics;

public class Scrapper{
    public static String scrapeGeniusLyrics(String page){
        if (page == null)
            return "";
        final String CONTAINER = "Lyrics__Container-sc";
        final String INSTRUMENTAL = "This song is an instrumental";
        int fakeContainer = page.indexOf(CONTAINER);
        if(fakeContainer == -1){
            return page.contains(INSTRUMENTAL) ? "This song is an instrumental" : "";
        }
        int lyricContainer = page.indexOf(CONTAINER, fakeContainer + 1);
        int textStart = page.indexOf('>', lyricContainer);
        StringBuilder lyrics = new StringBuilder();
        int angleBrackets = 0, divCounter = 1;
        boolean spaced = false;
        exitLoop:
        for (int i = textStart + 1, size = page.length(); i < size; i++){
            char chr = page.charAt(i);
            switch (chr){
                case '<':
                    //open + close
                    if (page.startsWith("br", i+1)){
                        i += 4;
                        if (page.charAt(i+3) == '/'){
                            i++;
                        }
                        if(!spaced)
                            lyrics.append('\n');
                        spaced = true;
                        continue;
                    }
                    if (page.startsWith("/div", i+1)){
                        if (divCounter == 0){
                            break exitLoop;
                        }
                        i += 4;
                        divCounter--;
                    }else if (page.startsWith("div", i + 1)){
                        i += 3;
                        divCounter++;
                    }
                    angleBrackets++;
                    break;
                case '&':
                    if (page.startsWith("#x27;", i + 1)){
                        i += 5;
                        lyrics.append('\'');
                    }else if(page.startsWith("quot;", i + 1)){
                        i += 5;
                        lyrics.append('"');
                    }else if(page.startsWith("apos;", i + 1)){
                        i += 5;
                        lyrics.append('\'');
                    }else if(page.startsWith("amp;", i+1)){
                        i += 4;
                        lyrics.append('&');
                    }
                    else{
                        lyrics.append('&');
                    }
                    break;
                case '>':
                    if (angleBrackets == 0){
                        break exitLoop;
                    }
                    angleBrackets--;
                    break;
                default:
                    if (angleBrackets == 0){
                        spaced = false;
                        lyrics.append(chr);
                    }
                    break;
            }
        }

        removeYouMightAlsoLike(lyrics);
        stripDigits(lyrics, 3);
        if(endsWith(lyrics, "Embed")){
            lyrics.setLength(lyrics.length() - 5);
        }
        stripDigits(lyrics, 4);
        if(endsWith(lyrics, "You might also like")){
            lyrics.setLength(lyrics.length() - 19);
        }
        return lyrics.toString();
    }

    private static void removeYouMightAlsoLike(StringBuilder lyrics){
        for (int i = 19; i < lyrics.length(); i++){
            //commonly found before opening square bracket [
            if(lyrics.charAt(i) == '[' && lyrics.substring(i-19, i).equals("You might also like")){
                lyrics.replace(i-19, i, "");
            }
        }
    }

    private static void stripDigits(StringBuilder str, final int quantity){
        if(quantity <= 0)
            return;
        int currLen = str.length();
        for (int i = currLen-1; i >= currLen - quantity && i > -1; i--){
            if(Character.isDigit(str.charAt(i))){
                str.setLength(i);
            }
        }
    }

    private static boolean endsWith(StringBuilder str, String seq){
        int mainLen = str.length();
        int start = str.length() - seq.length();
        if(start < 0){
            return false;
        }
        for (int i = start, j = 0; i < mainLen; i++, j++){
            if(str.charAt(i) != seq.charAt(j)){
                return false;
            }
        }
        return true;
    }
}
