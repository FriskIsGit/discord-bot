package bot.commands;

import bot.core.Bot;
import bot.utilities.jda.Actions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import no4j.core.Logger;

public abstract class Command {
    protected static Logger log;
    protected static JDA jda;
    protected static Actions actions;

    protected String[] aliases;
    protected String description = "";
    protected String usage = "";
    protected boolean enabled = true;
    protected boolean requiresAuth = false;
    protected boolean triggerableByBot = false;
    protected int timesExecuted = 0;

    public Command(String[] aliases) {
        if (aliases == null || aliases.length == 0) {
            throw new IllegalArgumentException("Aliases are either null or empty.");
        }
        this.aliases = aliases;
    }

    public static void initializeStaticMembers() {
        log = Logger.getLogger("primary");
        log.debug("Initializing static members");

        jda = Bot.getJDAInterface();
        if (jda == null) {
            throw new NullPointerException("Bot's JDA context is null");
        }
        actions = Bot.getActions();
        if (actions == null) {
            throw new NullPointerException("Bot's actions instance is null");
        }
    }

    //call execute to go through checks first
    public void execute(String commandName, MessageReceivedEvent message, String... args) {
        boolean authorAuthorized = isAuthorized(message.getAuthor().getIdLong());
        if (requiresAuth) {
            if (!authorAuthorized) {
                actions.messageChannel(message.getChannel(), "You must be authorized to use this command");
                return;
            }
        }

        if (!enabled && !authorAuthorized) {
            actions.messageChannel(message.getChannel(), "The command is not enabled at this time");
            return;
        }

        if (!triggerableByBot) {
            boolean isBot = message.getAuthor().isBot();
            if (isBot) {
                actions.messageChannel(message.getChannel(), "Bots cannot execute this command");
                return;
            }
        }
        executeImpl(commandName, message, args);
        timesExecuted++;
    }

    public boolean isAuthorized(long idLong) {
        return Bot.AUTHORIZED_USERS.contains(idLong);
    }

    public void executeUnrestricted(String commandName, String... args) {
        executeImpl(commandName, null, args);
        timesExecuted++;
    }

    protected abstract void executeImpl(String commandName, MessageReceivedEvent message, String... args);

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean mustBeAuthorized() {
        return requiresAuth;
    }

    public String getDescription() {
        return description;
    }

    public String setDescription() {
        return description;
    }
}
