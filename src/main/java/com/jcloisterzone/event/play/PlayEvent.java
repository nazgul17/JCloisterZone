package com.jcloisterzone.event.play;

import java.io.Serializable;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;

/**
 * Ancestor for all in-game event.
 */
@Immutable
public abstract class PlayEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long time;
    /** triggering player */
    private final Player triggeringPlayer;

    public PlayEvent(Player triggeringPlayer) {
        this.triggeringPlayer = triggeringPlayer;
        this.time = System.currentTimeMillis();
    }

    public Player getTriggeringPlayer() {
        return triggeringPlayer;
    }

    public long getTime() {
        return time;
    }
}
