package com.sucy.party.event;

import com.sucy.party.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeavePartyEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Party party;
    private final Player player;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public PlayerLeavePartyEvent(Party party, Player player) {
        this.party = party;
        this.player = player;
    }

    /**
     * @return the party the player left
     */
    public Party getParty() {
        return party;
    }

    /**
     * @return the player that left the party
     */
    public Player getPlayer() {
        return player;
    }
}
