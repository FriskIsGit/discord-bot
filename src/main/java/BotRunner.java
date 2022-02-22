import bot.deskort.Bot;

class BotRunner{
    public static void main(String[] args){
        try{
            boolean success = Bot.initialize();
            if(!success){
                return;
            }

        }catch (InterruptedException ignored){}
        System.out.println("Connected to: " + Bot.getServers().getServerNames());
        System.out.println("Threads active: " + Thread.activeCount());

        Thread chatThread = new Thread(() -> Bot.getActions().chatWithBot("bot"));
        chatThread.start();

    }
}
