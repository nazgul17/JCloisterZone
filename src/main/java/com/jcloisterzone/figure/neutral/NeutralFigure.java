package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Figure;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;

import io.vavr.collection.LinkedHashMap;

public class NeutralFigure<T extends BoardPointer> extends Figure<T> {

    private static final long serialVersionUID = 1L;

    public NeutralFigure(Game game) {
        super(game);
    }

    @SuppressWarnings("unchecked")
    public T getDeployment() {
        return (T) game.getDeployedNeutralFigures().get(this).getOrNull();
    }

    @Override
    public void deploy(T at) {
        T origin = getDeployment();
        game.replaceState(state -> {
            LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures = state.getDeployedNeutralFigures();
            return state.setDeployedNeutralFigures(deployedNeutralFigures.put(this, at));
        });
        game.post(new NeutralFigureMoveEvent(game.getActivePlayer(), this, origin, at));
    }


    public void undeploy() {
        deploy((T) null);
    }

    @Override
    public boolean at(Feature feature) {
        throw new UnsupportedOperationException("TODO IMMUTABLE");
    }

}
