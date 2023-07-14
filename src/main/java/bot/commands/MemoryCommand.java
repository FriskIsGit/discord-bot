package bot.commands;

import bot.music.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

import static bot.core.MessageProcessor.interactiveButtons;

public class MemoryCommand extends Command{
    private static final double MEGA_BYTE = 1024*1024D;

    public MemoryCommand(String... aliases){
        super(aliases);
        description = "Displays a memory management panel if no argument is provided\n" +
                      "Arguments: gc, clear_songs.";
        usage = "mem\n" +
                "mem gc\n" +
                "mem clear_songs";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(message == null){
            return;
        }
        MessageChannelUnion channel = message.getChannel();
        if(args.length == 0){
            channel.sendMessageEmbeds(createMemoryEmbed())
                    .setActionRow(interactiveButtons)
                    .queue();
            return;
        }
        switch (args[0]){
            case "clrsongs":
            case "clearsongs":
            case "clear_songs":
                AudioPlayer.clearAudioTracksFromMemory();
                break;
            case "gc":
                System.gc();
                break;
            default:
                channel.sendMessageEmbeds(createMemoryEmbed())
                        .setActionRow(interactiveButtons)
                        .queue();
                break;
        }
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
