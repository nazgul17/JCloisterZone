package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Game;

public class Count extends NeutralFigure<FeaturePointer> {

    private static final long serialVersionUID = 7549233370462508962L;

    @Override
    public void deploy(FeaturePointer at) {
        if (at != null && !at.getLocation().isCityOfCarcassonneQuarter()) {
            throw new IllegalArgumentException("Must be deployed on Quarter");
        }
        super.deploy(at);
    }

}
