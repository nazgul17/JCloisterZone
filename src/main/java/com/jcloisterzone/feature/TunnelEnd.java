package com.jcloisterzone.feature;

import com.jcloisterzone.PlayerAttributes;

public class TunnelEnd {

    public static enum TunnelToken {
        A, B
    }

    private final PlayerAttributes occupiedBy;
    private final TunnelToken token;

    public TunnelEnd() {
        this(null, null);
    }

    public TunnelEnd(PlayerAttributes occupiedBy, TunnelToken token) {
        this.occupiedBy = occupiedBy;
        this.token = token;
    }

    public PlayerAttributes getOccupiedBy() {
        return occupiedBy;
    }

    public TunnelToken getToken() {
        return token;
    }
}
