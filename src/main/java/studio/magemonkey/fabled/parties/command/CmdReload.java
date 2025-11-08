package studio.magemonkey.fabled.parties.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import studio.magemonkey.codex.mccore.commands.ConfigurableCommand;
import studio.magemonkey.codex.mccore.commands.IFunction;
import studio.magemonkey.fabled.parties.FabledParties;

/**
 * Command to reload the plugin's config.yml and language.yml
 */
public class CmdReload implements IFunction {
    private final static String MESSAGE = "The config.yml and language.yml have been reloaded";

    /**
     * Executes the command
     *
     * @param command owning command
     * @param plugin  plugin reference
     * @param sender  sender of the command
     * @param args    arguments provided
     * @param silent  whether to suppress output
     */
    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args, boolean silent) {
        FabledParties fabledParties = (FabledParties) plugin;
        fabledParties.loadConfiguration();

        if (!silent) {
            fabledParties.getLogger().info(MESSAGE);
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.GREEN + "[FabledParties] " + MESSAGE);
            }
        }
    }
}
