package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;

public interface FeaturesStateMixin {

    Map<FeaturePointer, Feature> getFeatureMap();
    GameState setFeatureMap(Map<FeaturePointer, Feature> featureMap);

     default Stream<Feature> getFeatures() {
        return Stream.ofAll(getFeatureMap().values())
            .distinct();
    }

    @SuppressWarnings("unchecked")
    default <T extends Feature> Stream<T> getFeatures(Class<T> cls) {
        return Stream.ofAll(getFeatureMap().values())
            .filter(Predicates.instanceOf(cls))
            .distinct()
            .map(f -> (T) f);
    }

}
