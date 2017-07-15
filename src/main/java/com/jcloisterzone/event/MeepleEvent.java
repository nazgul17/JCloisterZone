package com.jcloisterzone.event;

import com.jcloisterzone.IPlayer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;

public class MeepleEvent extends MoveEvent<FeaturePointer> {

    private final Meeple meeple;

    public MeepleEvent(IPlayer triggeringPlayer, Meeple meeple, FeaturePointer from, FeaturePointer to) {
        super(triggeringPlayer, from, to);
        this.meeple = meeple;
    }

    public Meeple getMeeple() {
        return meeple;
    }

    @Override
    public String toString() {
        return super.toString() + " meeple:" + meeple + " player:" + meeple.getPlayer().getNick();
    }
}
