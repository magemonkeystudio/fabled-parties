package com.sucy.party;

import com.sucy.party.command.*;
import com.sucy.party.hook.Hooks;
import com.sucy.party.mccore.PartyBoardManager;
import mc.promcteam.engine.mccore.commands.CommandManager;
import mc.promcteam.engine.mccore.commands.ConfigurableCommand;
import mc.promcteam.engine.mccore.commands.SenderType;
import mc.promcteam.engine.mccore.config.CommentedConfig;
import mc.promcteam.engine.mccore.config.CommentedLanguageConfig;
import mc.promcteam.engine.mccore.config.CustomFilter;
import mc.promcteam.engine.mccore.config.FilterType;
import mc.promcteam.engine.mccore.config.parse.DataSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Add-on plugin for SkillAPI allowing parties with shared experience
 */
public class Parties extends JavaPlugin {

    private final ArrayList<Party> parties = new ArrayList<>();
    private final List<Party> partiesRead = Collections.unmodifiableList(parties);
    private final ArrayList<String> toggled = new ArrayList<>();
    private CommentedLanguageConfig language;
    private UpdateTask task;
    private String sharing;
    private boolean removeOnDc;
    private boolean newLeaderOnDc;
    private boolean leaderInviteOnly;
    private boolean useScoreboard;
    private boolean levelScoreboard;
    private boolean debug;
    private double memberModifier;
    private double levelModifier;
    private long inviteTimeout;
    private int maxSize;

    /**
     * Loads settings and sets up the listeners
     */
    @Override
    public void onEnable() {
        task = new UpdateTask(this);

        loadConfiguration();

        new PartyListener(this);

        // Set up commands
        ConfigurableCommand root = new ConfigurableCommand(this, "pt", SenderType.ANYONE);
        root.addSubCommands(
                new ConfigurableCommand(this, "accept", SenderType.PLAYER_ONLY, new CmdAccept(), "Accepts a party request", "", PermissionNode.GENERAL),
                new ConfigurableCommand(this, "decline", SenderType.PLAYER_ONLY, new CmdDecline(), "Declines a party request", "", PermissionNode.GENERAL),
                new ConfigurableCommand(this, "info", SenderType.PLAYER_ONLY, new CmdInfo(), "Views party information", "", PermissionNode.GENERAL),
                new ConfigurableCommand(this, "invite", SenderType.PLAYER_ONLY, new CmdInvite(), "Invites a player to a party", "<player>", PermissionNode.GENERAL),
                new ConfigurableCommand(this, "leave", SenderType.PLAYER_ONLY, new CmdLeave(), "Leaves your party", "", PermissionNode.GENERAL),
                new ConfigurableCommand(this, "message", SenderType.PLAYER_ONLY, new CmdMsg(), "Sends a message to your party", "<message>", PermissionNode.GENERAL),
                new ConfigurableCommand(this, "toggle", SenderType.PLAYER_ONLY, new CmdToggle(), "Toggles party chat on/off", "", PermissionNode.GENERAL),
                new ConfigurableCommand(this, "reload", SenderType.ANYONE, new CmdReload(), "Reloads the plugin's config.yml and language.yml", "", PermissionNode.RELOAD)
                           );
        CommandManager.registerCommand(root);

        Hooks.init(this);
    }

    public void loadConfiguration() {
        CommentedConfig config = new CommentedConfig(this, "config");
        config.saveDefaultConfig();
        config.trim();
        config.checkDefaults();
        config.save();
        DataSection settings = config.getConfig();

        language = new CommentedLanguageConfig(this, "language");

        sharing = settings.getString("sharing", "none");
        removeOnDc = settings.getBoolean("remove-on-dc", false);
        newLeaderOnDc = settings.getBoolean("new-leader-on-dc", true);
        leaderInviteOnly = settings.getBoolean("only-leader-invites", true);
        useScoreboard = settings.getBoolean("use-scoreboard", false);
        levelScoreboard = settings.getBoolean("level-scoreboard", false);
        memberModifier = settings.getDouble("exp-modifications.members", 1.0);
        levelModifier = settings.getDouble("exp-modifications.level", 0.0);
        inviteTimeout = settings.getInt("invite-timeout", 30)*1000L;
        maxSize = settings.getInt("max-size", 4);
        debug = settings.getBoolean("debug-messages", false);
    }

    /**
     * Clears plugin data
     */
    @Override
    public void onDisable() {
        task.cancel();
        PartyBoardManager.clearBoards(this);
        HandlerList.unregisterAll(this);
        parties.clear();
    }

    /**
     * @return retrieves the type of sharing being used
     */
    public String getShareMode() {
        return sharing;
    }

    /**
     * @return whether or not party members are removed upon disconnect
     */
    public boolean isRemoveOnDc() {
        return removeOnDc;
    }

    /**
     * @return whether or not a new party leader is chosen upon disconnect
     */
    public boolean isNewLeaderOnDc() {
        return newLeaderOnDc;
    }

    /**
     * @return whether or not only the leader can invite new party members
     */
    public boolean isLeaderInviteOnly() {
        return leaderInviteOnly;
    }

    /**
     * @return whether or not scoreboards are being used
     */
    public boolean isUsingScoreboard() {
        return useScoreboard;
    }

    /**
     * @return whether or not levels are shown in the scoreboard over health
     */
    public boolean isLevelScoreboard() {
        return levelScoreboard;
    }

    /**
     * @return how long in milliseconds a party invitation lasts before expiring
     */
    public long getInviteTimeout() {
        return inviteTimeout;
    }

    /**
     * @return max size of the party
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @return the value for the member experience modifier
     */
    public double getMemberModifier() {
        return memberModifier;
    }

    /**
     * @return the value for the level experience modifier
     */
    public double getLevelModifier() {
        return levelModifier;
    }

    /**
     * Whether or not debug messages are enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Retrieves teh party the player is in
     *
     * @param player player to get for
     * @return joined party
     */
    public Party getJoinedParty(Player player) {
        for (Party party : parties) {
            if (party.isMember(player)) {
                return party;
            }
        }
        return null;
    }

    /**
     * Retrieves the party that the player is in or is invited to
     *
     * @param player player to check
     * @return party the player is in or null if not found
     */
    public Party getParty(Player player) {
        for (Party party : parties) {
            if (party.isMember(player) || party.isInvited(player)) {
                return party;
            }
        }
        return null;
    }

    /**
     * @return an unmodifiable list containing all the existing parties
     */
    public List<Party> getParties() {
        return partiesRead;
    }

    /**
     * Adds a new party to the list
     *
     * @param party party to add
     */
    public void addParty(Party party) {
        parties.add(party);
    }

    /**
     * Removes a party from the list
     *
     * @param party party to remove
     */
    public void removeParty(Party party) {
        parties.remove(party);
    }

    /**
     * Updates invitations for parties
     */
    public void update() {
        for (Party party : parties) {
            party.checkInvitations();
        }
    }

    /**
     * Checks if the player is toggled
     *
     * @param playerName name of the player to check
     * @return true if toggled, false otherwise
     */
    public boolean isToggled(String playerName) {
        return toggled.contains(playerName.toLowerCase());
    }

    /**
     * Toggles the player's party chat
     *
     * @param playerName name of player to toggle
     */
    public void toggle(String playerName) {
        if (isToggled(playerName)) {
            toggled.remove(playerName.toLowerCase());
        } else { toggled.add(playerName.toLowerCase()); }
    }

    /**
     * <p>Gets the message at the given key in the language configuration.</p>
     *
     * <p>Colors are applied and the message returned contains a list of all
     * lines from the configuration. If it was just a single value, there will
     * be only one element. If it was a string list, it will contain each line.</p>
     *
     * @param key     language key
     * @param player  whether or not the message is for a player
     * @param filters filters to apply
     * @return list of message lines
     */
    public List<String> getMessage(String key, boolean player, CustomFilter... filters) {
        return language.getMessage(key, player, FilterType.COLOR, filters);
    }

    /**
     * <p>Sends a message to the target based on the message at the given
     * language key.</p>
     *
     * @param target  recipient of the message
     * @param key     message key
     * @param filters filters to use
     */
    public void sendMessage(Player target, String key, CustomFilter... filters) {
        language.sendMessage(key, target, FilterType.COLOR, filters);
    }
}
