package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.ui.resources.DisplayableEntity;
import com.jcloisterzone.wsio.RmiProxy;


@DisplayableEntity("actions/towerpiece")
public class TowerPieceAction extends SelectTileAction {

    @Override
    public void perform(RmiProxy server, Position p) {
        server.placeTowerPiece(p);
    }

    @Override
    protected int getSortOrder() {
        return 20;
    }

    @Override
    protected Class<? extends ActionLayer<?>> getActionLayerType() {
        return TileActionLayer.class;
    }


    @Override
    public String toString() {
        return "place tower piece";
    }

}
