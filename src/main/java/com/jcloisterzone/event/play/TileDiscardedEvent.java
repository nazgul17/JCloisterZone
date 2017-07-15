package com.jcloisterzone.event.play;

import com.jcloisterzone.board.TileDefinition;

public class TileDiscardedEvent extends PlayEvent {

    private final TileDefinition tile;

    public TileDiscardedEvent(TileDefinition tile) {
        super(null);
        this.tile = tile;
    }

    public TileDefinition getTile() {
        return tile;
    }

}
