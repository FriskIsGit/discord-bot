package bot.deskort.commands.voice;

import bot.deskort.commands.Command;
import bot.music.AudioPlayer;
import bot.utilities.FileSeeker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;

public class SongsCommand extends Command{
    public SongsCommand(String... aliases){
        super(aliases);
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        AudioManager audioManager = message.getGuild().getAudioManager();
        AudioPlayer player = AudioPlayer.addSendingHandlerIfNull(audioManager);
        String[] fileNames = player.audioDirectory.list();
        if(fileNames != null && fileNames.length > 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Available tracks");
            embedBuilder.setColor(Color.BLUE);
            StringBuilder stringBuilder = new StringBuilder();
            int tracks = 0;
            for (String file : fileNames){
                String name = FileSeeker.getNameWithoutExtension(file);
                String ext = FileSeeker.getExtension(file);
                if(name.isEmpty() || ext.isEmpty() || !AudioPlayer.isExtensionSupported(ext)){
                    continue;
                }
                tracks++;
                stringBuilder.append(name);
                stringBuilder.append('\n');
                if(tracks%26 == 0){
                    embedBuilder.addField(new MessageEmbed.Field("", stringBuilder.toString(),true));
                    stringBuilder = new StringBuilder();
                }
            }
            if(stringBuilder.length() != 0){
                embedBuilder.addField(new MessageEmbed.Field("", stringBuilder.toString(),true));
            }
            actions.sendEmbed(message.getChannel(), embedBuilder.build());
        }
    }
}
