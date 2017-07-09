package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Set;

@LinkedImage("actions/takeprisoner")
public class TakePrisonerAction extends SelectFollowerAction {

    public TakePrisonerAction(Set<MeeplePointer> options) {
        super(options);
    }

    @Override
    public void perform(RmiProxy server, MeeplePointer mp) {
        server.takePrisoner(mp);
    }

    @Override
    public String toString() {
        return "take prisoner";
    }


}
