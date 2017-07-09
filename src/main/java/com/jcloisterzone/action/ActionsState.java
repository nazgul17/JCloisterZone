package com.jcloisterzone.action;

import io.vavr.collection.Vector;

public class ActionsState {

    private final Vector<PlayerAction<?>> actions;
    private final boolean passAllowed;

    public ActionsState(Vector<PlayerAction<?>> actions, boolean passAllowed) {
        super();
        this.actions = actions;
        this.passAllowed = passAllowed;
    }

    public Vector<PlayerAction<?>> getActions() {
        return actions;
    }

    public boolean isPassAllowed() {
        return passAllowed;
    }
}
