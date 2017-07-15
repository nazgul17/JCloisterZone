package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.event.play.PlayEvent;

import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Vector;

@Idempotent
@Deprecated
public class SelectActionEvent extends PlayEvent {

    private final boolean passAllowed;
    private final IndexedSeq<PlayerAction<?>> actions;


    public SelectActionEvent(Player targetPlayer, IndexedSeq<PlayerAction<?>> actions, boolean passAllowed) {
        super(null, targetPlayer);
        this.actions = actions;
        this.passAllowed = passAllowed;
        throw new UnsupportedOperationException("Replace with GameStateUpdate!");
    }

    public SelectActionEvent(Player player, PlayerAction<?> action, boolean passAllowed) {
        this(player, Vector.of(action), passAllowed);
    }

    public IndexedSeq<PlayerAction<?>> getActions() {
        return actions;
    }

    public boolean isPassAllowed() {
        return passAllowed;
    }

    @Override
    public String toString() {
        return super.toString() + " passAllowed:" + passAllowed + " actions:" + actions;
    }
}
