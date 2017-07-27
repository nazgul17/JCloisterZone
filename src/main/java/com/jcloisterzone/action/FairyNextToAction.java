package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedImage;

import io.vavr.collection.Set;

//TODO generic NeutralMeepleAction
@LinkedImage("actions/fairy")
public class FairyNextToAction extends SelectFollowerAction {

    public FairyNextToAction(Set<MeeplePointer> options) {
        super(options);
    }

    @Override
    public void perform(GameController gc, MeeplePointer target) {
        gc.getRmiProxy().moveNeutralFigure(target, Fairy.class);
    }

    @Override
    public String toString() {
        return "move fairy";
    }

}
