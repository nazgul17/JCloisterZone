package com.jcloisterzone.reducers;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.GameState;

import io.vavr.collection.LinkedHashMap;

public class DeployMeeple implements Reducer {

    private final Meeple meeple;
    private final FeaturePointer fp;

    public DeployMeeple(Meeple meeple, FeaturePointer fp) {
        this.meeple = meeple;
        this.fp = fp;
    }

    @Override
    public GameState apply(GameState state) {
        FeaturePointer origin = state.getDeployedMeeples().get(meeple).getOrNull();
        DeploymentCheckResult check = meeple.isDeploymentAllowed(state, fp);
        if (!check.result) {
          throw new IllegalArgumentException(check.error);
        }

        LinkedHashMap<Meeple, FeaturePointer> deployedMeeples = state.getDeployedMeeples();
        state = state.setDeployedMeeples(deployedMeeples.put(meeple, fp));
        state = state.setEvents(state.getEvents().append(
            new MeepleEvent(state.getActivePlayer(), meeple, origin, fp)
        ));
        return state;
    }

}
