package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.TowerCapability;

public class TowerIncreasedEvent extends PlayEvent {

    private final int captureRange;
    private final Position position;

    public TowerIncreasedEvent(Player triggeringplayer, Position position, int captureRange) {
        super(triggeringplayer, null);
        this.captureRange = captureRange;
        this.position = position;
    }

    public int getCaptureRange() {
        return captureRange;
    }

    public Position getPosition() {
        return position;
    }
}
