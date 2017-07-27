package com.jcloisterzone.action;

import java.io.Serializable;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;

import io.vavr.collection.Vector;

@Immutable
public class ActionsState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Player player;
    private final Vector<PlayerAction<?>> actions;
    private final boolean passAllowed;

    public ActionsState(Player player, Vector<PlayerAction<?>> actions, boolean passAllowed) {
        this.player = player;
        this.actions = actions;
        this.passAllowed = passAllowed;
    }

    public ActionsState(Player player, PlayerAction<?> action, boolean passAllowed) {
        this(player, Vector.of(action), passAllowed);
    }

    public ActionsState setActions(Vector<PlayerAction<?>> actions) {
        return new ActionsState(player, actions, passAllowed);
    }

    public ActionsState appendAction(PlayerAction<?> action) {
        return setActions(actions.append(action));
    }

    public Player getPlayer() {
        return player;
    }

    public Vector<PlayerAction<?>> getActions() {
        return actions;
    }

    public boolean isPassAllowed() {
        return passAllowed;
    }
}
