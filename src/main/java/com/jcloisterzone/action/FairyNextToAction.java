package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.ui.resources.DisplayableEntity;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Set;

//TODO generic NeutralMeepleAction
@DisplayableEntity("actions/fairy")
public class FairyNextToAction extends SelectFollowerAction {

    public FairyNextToAction(Set<MeeplePointer> options) {
        super(options);
    }

    @Override
    protected int getSortOrder() {
        return 30;
    }

    @Override
    public void perform(RmiProxy server, MeeplePointer target) {
        server.moveNeutralFigure(target, Fairy.class);
    }

    @Override
    public String toString() {
        return "move fairy";
    }

}
