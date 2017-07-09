package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Set;

@LinkedImage("actions/castle")
public class CastleAction extends SelectFeatureAction {

    public CastleAction(Set<FeaturePointer> options) {
        super(options);
    }

    public void perform(RmiProxy server, FeaturePointer bp) {
        server.deployCastle(bp.getPosition(), bp.getLocation());
    }

    @Override
    public String toString() {
        return "place castle";
    }

}
