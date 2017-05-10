package com.jcloisterzone.ui.grid.layer;

import java.util.HashMap;
import java.util.Map;

import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;

public class BarnAreaLayer extends AbstractAreaLayer<BarnAction> {

    public BarnAreaLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    protected Map<BoardPointer, FeatureArea> prepareAreas() {
        BarnAction action = getAction();
        Map<BoardPointer, FeatureArea> result = new HashMap<>();

        Tile tile = getGame().getCurrentTile();
        Position pos = tile.getPosition();
        Map<Location, FeatureArea> locMap = rm.getBarnTileAreas(tile, getTileWidth(), getTileHeight(), action.getLocations(pos));
        addAreasToResult(result, locMap, pos, getTileWidth(), getTileHeight());
        return result;
    }

    @Override
    protected void performAction(BoardPointer fp) {
        getAction().perform(getRmiProxy(), (FeaturePointer) fp);
    }


}
