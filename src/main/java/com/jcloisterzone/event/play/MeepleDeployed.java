package com.jcloisterzone.event.play;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;

public class MeepleDeployed extends PlayEvent {

    private Meeple meeple;
    private BoardPointer ptr;

    public MeepleDeployed(Player triggeringPlayer, Meeple meeple, BoardPointer ptr) {
        super(triggeringPlayer);
        this.meeple = meeple;
        this.ptr = ptr;
    }

    public Meeple getMeeple() {
        return meeple;
    }

    public BoardPointer getPointer() {
        return ptr;
    }

    public Location getLocation() {
        return ptr.asFeaturePointer().getLocation();
    }


}
