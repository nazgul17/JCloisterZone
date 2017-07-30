package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedImage;

import io.vavr.collection.Set;

@LinkedImage("actions/princess")
public class PrincessAction extends SelectFollowerAction {

    public PrincessAction(Set<MeeplePointer> options) {
        super(options);
    }

    @Override
    public void perform(GameController gc, MeeplePointer ptr) {
        gc.getRmiProxy().undeployMeeple(ptr);
    }

    @Override
    public String toString() {
        return "undeploy with princesss";
    }
}
