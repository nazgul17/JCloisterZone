package com.jcloisterzone.event.play;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;

public class TilePlacedEvent extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private final TileDefinition tile;
    private final Position position;
    private final Rotation rotation;

    public TilePlacedEvent(Player triggeringPlayer, TileDefinition tile, Position position, Rotation rotation) {
        super(triggeringPlayer);
        this.tile = tile;
        this.position = position;
        this.rotation = rotation;
    }

    public TileDefinition getTile() {
        return tile;
    }

    public Position getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }
}
