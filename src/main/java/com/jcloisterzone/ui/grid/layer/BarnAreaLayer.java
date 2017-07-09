package com.jcloisterzone.ui.grid.layer;

import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class BarnAreaLayer extends AbstractAreaLayer {

    public BarnAreaLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public BarnAction getAction() {
        return (BarnAction) super.getAction();
    }

    @Override
    protected Map<BoardPointer, FeatureArea> prepareAreas() {
        BarnAction action = getAction();
        Map<BoardPointer, FeatureArea> result = HashMap.empty();

        Tile tile = getGame().getCurrentTile();
        Position pos = tile.getPosition();
        Map<Location, FeatureArea> locMap = rm.getBarnTileAreas(tile, getTileWidth(), getTileHeight(), action.getLocations(pos));
        return addAreasToResult(result, locMap, pos, getTileWidth(), getTileHeight());
    }

    @Override
    protected void performAction(BoardPointer fp) {
        getAction().perform(getRmiProxy(), (FeaturePointer) fp);
    }


}
