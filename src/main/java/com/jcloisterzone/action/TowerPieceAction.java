package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Set;


@LinkedImage("actions/towerpiece")
@LinkedGridLayer(TileActionLayer.class)
public class TowerPieceAction extends SelectTileAction {

    public TowerPieceAction(Set<Position> options) {
        super(options);
    }

    @Override
    public void perform(RmiProxy server, Position p) {
        server.placeTowerPiece(p);
    }

    @Override
    public String toString() {
        return "place tower piece";
    }

}
