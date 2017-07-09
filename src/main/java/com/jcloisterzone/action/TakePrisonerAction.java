package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.resources.DisplayableEntity;
import com.jcloisterzone.wsio.RmiProxy;

@DisplayableEntity("actions/takeprisoner")
public class TakePrisonerAction extends SelectFollowerAction {

    @Override
    public void perform(RmiProxy server, MeeplePointer mp) {
        server.takePrisoner(mp);
    }

    @Override
    public String toString() {
        return "take prisoner";
    }


}
