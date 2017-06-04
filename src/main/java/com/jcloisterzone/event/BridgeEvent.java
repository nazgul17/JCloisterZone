package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BridgeCapability;

public class BridgeEvent extends FeatureEvent {

    public static final int DEPLOY = 1;
    public static final int REMOVE = 2;

    boolean forced; //if force by tile placement

    public BridgeEvent(int type, Player triggeringPlayer, Position position, Location location) {
        super(type, triggeringPlayer, new FeaturePointer(position, location));
    }

    public boolean isForced() {
        return forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }
}
