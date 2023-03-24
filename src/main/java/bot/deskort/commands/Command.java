package bot.deskort.commands;

import bot.deskort.Bot;
import bot.utilities.Actions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command{
    protected static JDA jda;
    protected static Actions actions;
    protected String[] aliases;
    protected String description = "";
    protected boolean enabled = true;
    protected boolean requiresAuth = false;
    protected boolean triggerableByBot = false;
    protected int timesExecuted = 0;

    public Command(String[] aliases){
        if(aliases == null || aliases.length == 0){
            throw new IllegalArgumentException("Aliases are either null or empty.");
        }
        this.aliases = aliases;
    }

    public static void initializeStaticMembers(){
        jda = Bot.getJDAInterface();
        if(jda == null){
            throw new NullPointerException("Bot's JDA context is null");
        }
        actions = Bot.getActions();
        if(actions == null){
            throw new NullPointerException("Bot's actions instance is null");
        }
    }

    //call execute to go through checks first
    public void execute(String commandName, MessageReceivedEvent message, String... args){
        if(!enabled){
            actions.messageChannel(message.getChannel(), "The command is not enabled at this time");
            return;
        }
        if(requiresAuth){
            boolean authorAuthorized = Bot.AUTHORIZED_USERS.contains(message.getAuthor().getIdLong());
            if(!authorAuthorized){
                actions.messageChannel(message.getChannel(), "You must be authorized to use this command");
                return;
            }
        }
        if(!triggerableByBot){
            boolean isBot = message.getAuthor().isBot();
            if(isBot){
                actions.messageChannel(message.getChannel(), "Bots cannot execute this command");
                return;
            }
        }
        executeImpl(commandName, message, args);
        timesExecuted++;
    }
    protected abstract void executeImpl(String commandName, MessageReceivedEvent message, String... args);

    public boolean isEnabled(){
        return enabled;
    }

    public void enable(){
        enabled = true;
    }

    public void disable(){
        enabled = false;
    }

    public boolean mustBeAuthorized(){
        return requiresAuth;
    }

    public String getDescription(){
        return description;
    }
    public String setDescription(){
        return description;
    }
}
