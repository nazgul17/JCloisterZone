package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.resources.DisplayableEntity;
import com.jcloisterzone.wsio.RmiProxy;

//TODO generic token action ?

@DisplayableEntity("actions/bridge")
public class BridgeAction extends SelectFeatureAction {

    @Override
    public void perform(RmiProxy server, FeaturePointer bp) {
        server.deployBridge(bp.getPosition(), bp.getLocation());
    }

    @Override
    public String toString() {
        return "place bridge";
    }

}
