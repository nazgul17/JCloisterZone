package com.jcloisterzone.game.state;

import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;

import io.vavr.Predicates;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public interface GameStateHelpers {

    // Actions

    default PlayerAction<?> getAction(GameState state) {
        ActionsState as = state.getPlayerActions();
        return as == null ? null : as.getActions().get();
    }

    default GameState appendAction(GameState state, PlayerAction<?> action) {
        assert action != null;
        ActionsState as = state.getPlayerActions();
        return state.setPlayerActions(as.appendAction(action));
    }

    // Features

    default Stream<Feature> getFeatures(GameState state) {
        return Stream.ofAll(state.getFeatures().values())
            .distinct();
    }

    @SuppressWarnings("unchecked")
    default <T extends Feature> Stream<T> getFeatures(GameState state, Class<T> cls) {
        return Stream.ofAll(state.getFeatures().values())
            .filter(Predicates.instanceOf(cls))
            .distinct()
            .map(f -> (T) f);
    }

    // Capabilities

    default boolean hasCapability(GameState state, Class<? extends Capability<?>> cls) {
        return state.getCapabilities().contains(cls);
    }

}
