package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;

import io.vavr.collection.Set;

//TODO generic NeutralMeepleAction ?
@LinkedImage("actions/dragonmove")
@LinkedGridLayer(TileActionLayer.class)
public class MoveDragonAction extends SelectTileAction {

    public MoveDragonAction(Set<Position> options) {
        super(options);
    }

    @Override
    public void perform(GameController gc, Position p) {
        gc.getRmiProxy().moveNeutralFigure(p.asFeaturePointer(), Dragon.class);
    }

    @Override
    public String toString() {
        return "move dragon";
    }

}