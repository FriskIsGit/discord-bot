package bot.deskort.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;

public class RussianCommand extends Command{
    public RussianCommand(String... aliases){
        super(aliases);
        description = "Кonvэгт тэхт тo гцslаи";
        usage = "ru `text`";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 0){
            return;
        }
        String text = String.join(" ", args);
        int textLen = text.length();
        StringBuilder converted = new StringBuilder(textLen);
        for (int i = 0; i < textLen; i++){
            if(i+3 <= textLen){
                String three = text.substring(i, i+3);
                String mapping = tripleLetter.get(three);
                if(mapping != null){
                    converted.append(mapping);
                    i += 2;
                    continue;
                }
            }

            if(i+2 <= textLen){
                String two = text.substring(i, i+2);
                String mapping = doubleLetter.get(two);
                if(mapping != null){
                    converted.append(mapping);
                    i++;
                    continue;
                }
            }

            if(i+1 <= textLen){
                String one = text.substring(i, i+1);
                String mapping = singleLetter.get(one);
                if(mapping != null){
                    converted.append(mapping);
                    continue;
                }
                converted.append(one);
            }

        }
        int currLen = converted.length();
        if(currLen > 2 && converted.charAt(currLen-1) == 'э'){
            converted.setLength(currLen-1);
        }
        actions.messageChannel(message.getChannel(), converted.toString());
    }

    private static final HashMap<String, String> tripleLetter = new HashMap<String, String>(){{
        put("You", "Ю");
        put("you", "ю");
        put("Sch", "Ш");
        put("sch", "ш");
    }};
    private static final HashMap<String, String> doubleLetter = new HashMap<String, String>(){{
        put("Ya", "Я");
        put("Ja", "Я");
        put("ya", "я");
        put("ja", "я");
        put("je", "е");
        put("ye", "е");
        put("ie", "е");
        put("Sh", "Ш");
        put("sh", "ш");
        put("Sz", "Ш");
        put("sz", "ш");
    }};

    private static final HashMap<String, String> singleLetter = new HashMap<String, String>(){{
        put("B","Б");
        put("b","б");
        put("E","Э");
        put("e","э");
        put("V","В");
        put("v","в");
        put("G","Г");
        put("g","г");
        put("D","Д");
        put("d","д");
        put("Ż","Ж");
        put("ż","ж");
        put("ž","ж");
        put("Z","З");
        put("z","з");
        put("I","И");
        put("i","и");
        put("J","Й");
        put("j","й");
        put("K","К");
        put("k","к");
        put("L","Ль");
        put("l","ль");
        put("N","Н");
        put("n","н");
        put("P","П");
        put("p","п");
        put("R","Р");
        put("r","р");
        put("S","С");
        put("s","с");
        put("U","У");
        put("u","у");
        put("Õ","У");
        put("õ","õ");
        put("F","Ф");
        put("f","ф");
        put("T","Т");
        put("t","т");
        put("H","Х");
        put("h","х");
        put("C","Ц");
        put("c","ц");
        put("č","Ч");
        put("ć","Ч");
        put("Y","Ы");
        put("y","ы");
        put("â","я");
        put("û","ю");
        put("Ł","Л");
        put("ł","л");
        put("Ę","Э");
        put("ę","э");
    }};
}
