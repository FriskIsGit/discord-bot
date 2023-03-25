import bot.deskort.Bot;

class BotRunner{
    public static void main(String[] args){
        try{
            Bot.initialize();
        }catch (InterruptedException ignored){}
        System.out.println("Connected to: " + Bot.getActions().getServerNames());
        System.out.println("Respective ids: " + Bot.getActions().getServerIds());
        System.out.println("Threads active: " + Thread.activeCount());
        System.out.println("Bot prefix: " + Bot.PREFIX);

        Thread chatThread = new Thread(() -> Bot.getActions().chatWithBot("bot"));
        chatThread.start();
    }
}
