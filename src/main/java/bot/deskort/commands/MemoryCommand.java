package bot.deskort.commands;

import bot.music.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

import static bot.deskort.MessageProcessor.interactiveButtons;

public class MemoryCommand extends Command{
    private static final double MEGA_BYTE = 1024*1024D;
    public MemoryCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        message.getChannel()
                .sendMessageEmbeds(createMemoryEmbed())
                .setActionRow(interactiveButtons)
                .queue();
    }
    public static MessageEmbed createMemoryEmbed(){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Memory stats");
        embedBuilder.setColor(Color.BLACK);
        embedBuilder.addField(new MessageEmbed.Field("", getUsedRuntimeMemoryAsString(),false));
        embedBuilder.addField(new MessageEmbed.Field("", getSongsSizeInMemoryAsString(),false));
        return embedBuilder.build();
    }
    private static String getUsedRuntimeMemoryAsString(){
        Runtime runtime = Runtime.getRuntime();
        long memUsed = runtime.totalMemory() - runtime.freeMemory();
        String valueFormatted = formatDouble(memUsed / MEGA_BYTE);
        return "Total mem - free mem: ```" + valueFormatted + " MBs ```";
    }
    private static String getSongsSizeInMemoryAsString(){
        String valueFormatted =  formatDouble(AudioPlayer.getSongsSizeInMemory() / MEGA_BYTE);
        return "Songs size in memory: ```" + valueFormatted + " MBs ```";
    }
    private static String formatDouble(double value){
        return String.format("%.2f", value);
    }
}
