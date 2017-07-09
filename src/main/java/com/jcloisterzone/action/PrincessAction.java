package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.resources.DisplayableEntity;
import com.jcloisterzone.wsio.RmiProxy;

@DisplayableEntity("actions/princess")
public class PrincessAction extends SelectFollowerAction {

    @Override
    public void perform(RmiProxy server, MeeplePointer mp) {
        server.undeployMeeple(mp);
    }

    @Override
    protected int getSortOrder() {
        return 1;
    }

    @Override
    public String toString() {
        return "undeploy with princesss";
    }
}
