package com.jcloisterzone.event;

import com.jcloisterzone.IPlayer;

/**
 * Ancestor for all in-game event.
 */
public abstract class PlayEvent extends Event {

    private final IPlayer triggeringPlayer;
    private final IPlayer targetPlayer;

    public PlayEvent(IPlayer triggeringPlayer, IPlayer targetPlayer) {
        this(0, triggeringPlayer, targetPlayer);
    }

    public PlayEvent(int type, IPlayer triggeringPlayer, IPlayer targetPlayer) {
        super(type);
        this.triggeringPlayer = triggeringPlayer;
        this.targetPlayer = targetPlayer;
    }

    public IPlayer getTargetPlayer() {
        return targetPlayer;
    }

    public IPlayer getTriggeringPlayer() {
        return triggeringPlayer;
    }
}
