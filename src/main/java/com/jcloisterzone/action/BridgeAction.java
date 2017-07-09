package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Set;

//TODO generic token action ?

@LinkedImage("actions/bridge")
public class BridgeAction extends SelectFeatureAction {

    public BridgeAction(Set<FeaturePointer> options) {
        super(options);
    }

    @Override
    public void perform(RmiProxy server, FeaturePointer bp) {
        server.deployBridge(bp.getPosition(), bp.getLocation());
    }

    @Override
    public String toString() {
        return "place bridge";
    }

}
