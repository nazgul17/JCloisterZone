package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.grid.layer.BarnAreaLayer;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Set;

//TODO do not extends select feature, use special type for corner based on position
@LinkedImage("actions/barn")
@LinkedGridLayer(BarnAreaLayer.class)
public class BarnAction extends SelectFeatureAction {

    public BarnAction(Set<FeaturePointer> options) {
        super(options);
    }

    @Override
    public void perform(RmiProxy server, FeaturePointer bp) {
        server.deployMeeple(bp, Barn.class);
    }


    @Override
    public String toString() {
        return "place barn";
    }
}
