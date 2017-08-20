package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Predicates;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;

public interface BoardMixin {

    LinkedHashMap<Position, PlacedTile> getPlacedTiles();
    GameState setPlacedTiles(LinkedHashMap<Position, PlacedTile> placedTiles);

    Map<FeaturePointer, Feature> getFeatureMap();
    GameState setFeatureMap(Map<FeaturePointer, Feature> featureMap);


    // Tiles

//    default Stream<Tuple2<Location, TileDefinition>> getAdjacentTiles(Position pos) {
//        return Stream.ofAll(Position.ADJACENT)
//            .map(locPoc -> locPos.map2(offset -> getPlacedTiles(pos.add(offset))))
//            .filter(locTile -> locTile._2 != null);
//
//    }

    // Features

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
