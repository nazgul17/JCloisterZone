package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.MeepleReturned;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Stream;

public class UndeployMeeple implements Reducer {

    private final Meeple meeple;

    public UndeployMeeple(Meeple meeple) {
        this.meeple = meeple;
    }

    @Override
    public GameState apply(GameState state) {
        FeaturePointer source = meeple.getDeployment(state);
        assert source != null;

        PlayEventMeta metaWithPlayer = PlayEventMeta.createWithActivePlayer(state);
        state = primaryUndeploy(state, metaWithPlayer, meeple, source);
        Player owner = meeple.getPlayer();

        // Undeploy lonely Builders and Pigs
        PlayEventMeta metaNoPlayer = PlayEventMeta.createWithoutPlayer();
        Feature feature = state.getFeature(source);
        Stream<Tuple2<Meeple, FeaturePointer>> threatened = feature.getMeeples2(state)
            .filter(m -> (m._1 instanceof Pig) || (m._1 instanceof Builder))
            .filter(m -> m._1.getPlayer().equals(owner));

        for (Tuple2<Meeple, FeaturePointer> t : threatened) {
            if (feature.getFollowers(state).find(f -> f.getPlayer().equals(owner)).isEmpty()) {
                state = undeploy(state, metaNoPlayer, t._1, t._2);
            }
        }

        return state;
    }

    protected GameState primaryUndeploy(GameState state, PlayEventMeta meta, Meeple meeple, FeaturePointer source) {
        return undeploy(state, meta, meeple, source);
    }

    private GameState undeploy(GameState state, PlayEventMeta meta, Meeple meeple, FeaturePointer source) {
        LinkedHashMap<Meeple, FeaturePointer> deployedMeeples = state.getDeployedMeeples();
        state = state.setDeployedMeeples(deployedMeeples.remove(meeple));
        state = state.appendEvent(
            new MeepleReturned(meta, meeple, source)
        );
        return state;
    }

}
