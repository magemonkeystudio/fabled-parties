package com.promcteam.fabled.parties.command;

import com.promcteam.fabled.parties.FabledParties;
import com.promcteam.fabled.parties.Party;
import com.promcteam.fabled.parties.lang.ErrorNodes;
import com.promcteam.fabled.parties.lang.IndividualNodes;
import com.promcteam.codex.mccore.commands.ConfigurableCommand;
import com.promcteam.codex.mccore.commands.IFunction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Command to toggle party chat
 */
public class CmdToggle implements IFunction {

    /**
     * Executes the command
     *
     * @param command owning command
     * @param plugin  plugin reference
     * @param sender  sender of the command
     * @param args    arguments provided
     */
    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args) {

        FabledParties fabledParties = (FabledParties) plugin;
        Player        player        = (Player) sender;

        // Check the sender's party status
        Party party = fabledParties.getParty(player);
        if (party != null && !party.isEmpty()) {

            fabledParties.toggle(player.getName());
            if (fabledParties.isToggled(player.getName())) {
                fabledParties.sendMessage(player, IndividualNodes.CHAT_ON);
            } else {
                fabledParties.sendMessage(player, IndividualNodes.CHAT_OFF);
            }
        }

        // Not in a party
        else {
            fabledParties.sendMessage(player, ErrorNodes.NO_PARTY);
        }
    }
}
