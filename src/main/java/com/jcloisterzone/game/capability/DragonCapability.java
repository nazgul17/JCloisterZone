package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.reducers.MoveNeutralFigure;

import io.vavr.collection.Vector;

@Immutable
public class DragonCapability extends Capability {

    private static final long serialVersionUID = 1L;

    public static final int DRAGON_MOVES = 6;

    private final Vector<Position> dragonMoves;

    public DragonCapability() {
        this(Vector.empty());
    }

    public DragonCapability(Vector<Position> dragonMoves) {
        this.dragonMoves = dragonMoves;
    }

    public DragonCapability setDragonMoves(Vector<Position> dragonMoves) {
        return new DragonCapability(dragonMoves);
    }

    public Vector<Position> getDragonMoves() {
        return dragonMoves;
    }

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("volcano").getLength() > 0) {
            tile = tile.setTileTrigger(TileTrigger.VOLCANO);
        }
        if (xml.getElementsByTagName("dragon").getLength() > 0) {
            tile = tile.setTileTrigger(TileTrigger.DRAGON);
        }
        return tile;
    }

    @Override
    public String getTileGroup(TileDefinition tile) {
        return tile.getTrigger() == TileTrigger.DRAGON ? "dragon" : null;
    }


    @Override
    public GameState onStartGame(GameState state) {
        return state.setNeutralFigures(
            state.getNeutralFigures().setDragon(new Dragon("dragon.1"))
        );
    }

    @Override
    public GameState onTilePlaced(GameState state) {
        Tile tile = state.getBoard().getLastPlaced();
        if (tile.hasTrigger(TileTrigger.VOLCANO)) {
            state = state.setTilePack(
                state.getTilePack().setGroupState("dragon", TileGroupState.ACTIVE)
            );
            state = (
                new MoveNeutralFigure<>(state.getNeutralFigures().getDragon(), tile.getPosition())
            ).apply(state);
        }
        return state;
    }

    @Override
    public boolean isDeployAllowed(GameState state, Position pos) {
        return !pos.equals(state.getNeutralFigures().getDragonDeployment());
    }
}
