package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Set;

public class MeepleAction extends SelectFeatureAction {

    private static final long serialVersionUID = 1L;

    private final Class<? extends Meeple> meepleType;

    public MeepleAction(Class<? extends Meeple> meepleType, Set<FeaturePointer> options) {
        super(options);
        this.meepleType = meepleType;
    }

    public Class<? extends Meeple> getMeepleType() {
        return meepleType;
    }

    @Override
    public void perform(GameController gc, FeaturePointer bp) {
        RmiProxy server = gc.getRmiProxy();
        server.deployMeeple(bp, meepleType);
    }

    @Override
    public String toString() {
        return "place " + meepleType.getSimpleName() + " ? " + getOptions();
    }
}
