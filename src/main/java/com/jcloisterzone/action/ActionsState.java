package com.jcloisterzone.action;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerAttributes;

import io.vavr.collection.Vector;

public class ActionsState {

    private final PlayerAttributes player;
    private final Vector<PlayerAction<?>> actions;
    private final boolean passAllowed;

    public ActionsState(PlayerAttributes player, Vector<PlayerAction<?>> actions, boolean passAllowed) {
        this.player = player;
        this.actions = actions;
        this.passAllowed = passAllowed;
    }

    public ActionsState(Player player, Vector<PlayerAction<?>> actions, boolean passAllowed) {
        this(player.asPlayerAttributes(), actions, passAllowed);
    }

    public PlayerAttributes getPlayer() {
        return player;
    }

    public Vector<PlayerAction<?>> getActions() {
        return actions;
    }

    public boolean isPassAllowed() {
        return passAllowed;
    }
}
