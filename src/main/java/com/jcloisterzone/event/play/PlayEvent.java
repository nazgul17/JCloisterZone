package com.jcloisterzone.event.play;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.event.Event;

/**
 * Ancestor for all in-game event.
 */
@Immutable
public abstract class PlayEvent {

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
