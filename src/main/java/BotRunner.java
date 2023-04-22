import bot.deskort.Bot;
import bot.utilities.jda.ConsoleChat;

class BotRunner{
    public static void main(String[] args){
        try{
            Bot.initialize();
        }catch (InterruptedException ignored){}
        System.out.println("Connected to: " + Bot.getActions().getServerNames());
        System.out.println("Respective ids: " + Bot.getActions().getServerIds());
        System.out.println("Threads active: " + Thread.activeCount());
        System.out.println("Bot prefix: " + Bot.PREFIX);

        Thread chatThread = new Thread(() -> new ConsoleChat().beginChat());
        chatThread.start();
    }
}
