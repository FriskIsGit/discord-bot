package bot.utilities;

import bot.deskort.Bot;

import java.util.Timer;
import java.util.TimerTask;

public class ShutdownTimer {
    private static final char[] supportedUnits = {'s','m','h','d'};
    private Timer timer;
    private TimerTask shutdownTask;

    public ShutdownTimer(){
        renewClocks();
    }

    private boolean scheduled = false;

    public void countdown(int delayInSeconds){
        if(scheduled){
            abort();
        }
        timer.schedule(shutdownTask, delayInSeconds*1000L);
        scheduled = true;
    }
    public void abort(){
        timer.cancel();
        scheduled = false;
        renewClocks();
    }
    public boolean isScheduled(){
        return scheduled;
    }

    private void renewClocks(){
        shutdownTask = new TimerTask(){
            @Override
            public void run(){
                System.out.println("Shutting down");
                Bot.getJDAInterface().shutdown();
                System.exit(0);
            }
        };
        timer = new Timer();
    }

    /**
     * @param time - period expressed as <number><unit>
     * (e.g. 12h, 4m, 7s, 2d), with support for days, hours, minutes and seconds
     * @return seconds value as long or -1 on failure to parse
     */
    public static int parseToSeconds(String time){
        if(time.length() < 2 || !Character.isDigit(time.charAt(0)) ){
            return -1;
        }
        int unitIndex = -1;
        char[] arr = time.toCharArray();
        for (int i = 1; i < arr.length; i++){
            if(Character.isLetter(arr[i]) && isSupported(arr[i])){
                unitIndex = i;
                break;
            }
        }
        if(unitIndex == -1){
            return -1;
        }
        String numberAsString = time.substring(0, unitIndex);
        int num;
        try{
            num = Integer.parseInt(numberAsString);
        }catch (NumberFormatException nfExc){
            return -1;
        }
        switch (arr[unitIndex]){
            case 's':
                return num;
            case 'm':
                return num*60;
            case 'h':
                return num*3600;
            case 'd':
                return num*86400;
        }
        return -1;
    }
    private static boolean isSupported(char c){
        for (char unit : supportedUnits){
            if(c == unit){
                return true;
            }
        }
        return false;
    }
}
