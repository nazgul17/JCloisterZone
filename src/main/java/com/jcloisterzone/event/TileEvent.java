package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;

public class TileEvent extends PlayEvent {

    public static final int DRAW = 1;
    public static final int PLACEMENT = 2;
    public static final int DISCARD = 3;
    public static final int REMOVE = 4;

    private final TileDefinition tile;
    private final Position position;
    private final Rotation rotation;

    public TileEvent(int type, Player player, TileDefinition tile, Position position, Rotation rotation) {
        super(type, player, type == DRAW ? player : null);
        this.tile = tile;
        this.position = position;
        this.rotation = rotation;
    }

    public TileDefinition getTileDefinition() {
        return tile;
    }

    public Position getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return super.toString() + " tile:" + tile.getId() + " position:" + position;
    }

}
