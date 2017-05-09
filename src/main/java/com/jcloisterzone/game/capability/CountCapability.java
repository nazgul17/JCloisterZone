package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class CountCapability extends Capability {

    private static final String[] FORBIDDEN_TILES = new String[] { "CO.6", "CO.7" };

    private Count count;

    public CountCapability(Game game) {
        super(game);
        count = new Count(game);
        game.getNeutralFigures().add(count);
    }

    public static boolean isTileForbidden(Tile tile) {
        String id = tile.getId();
        for (String forbidden : FORBIDDEN_TILES) {
            if (forbidden.equals(id)) return true;
        }
        return false;
    }

    @Override
    public boolean isDeployAllowed(Tile tile, Class<? extends Meeple> meepleType) {
        return !isTileForbidden(tile);
    }

    @Override
    public void handleEvent(Event event) {
       if (event instanceof TileEvent) {
           tilePlaced((TileEvent) event);
       }
    }

    private void tilePlaced(TileEvent ev) {
        Tile tile = ev.getTile();
        if (ev.getType() == TileEvent.PLACEMENT && "CO.7".equals(tile.getId())) {
            count.deploy(new FeaturePointer(tile.getPosition(), Location.QUARTER_CASTLE));
        }
    }
}
