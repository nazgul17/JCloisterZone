package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Set;

//TODO generic NeutralMeepleAction
public class MageAndWitchAction extends SelectFeatureAction {

    private final boolean mage;

    public MageAndWitchAction(Set<FeaturePointer> options, boolean mage) {
        super(options);
        this.mage = mage;
    }

    public boolean isMage() {
        return mage;
    }

    @Override
    public void perform(RmiProxy server, FeaturePointer target) {
        if (mage) {
            server.moveNeutralFigure(target, Mage.class);
        } else {
            server.moveNeutralFigure(target, Witch.class);
        }
    }
}
