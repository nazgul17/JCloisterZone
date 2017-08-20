package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;

public interface BoardMixin {

    LinkedHashMap<Position, PlacedTile> getPlacedTiles();
    GameState setPlacedTiles(LinkedHashMap<Position, PlacedTile> placedTiles);

    Map<FeaturePointer, Feature> getFeatureMap();
    GameState setFeatureMap(Map<FeaturePointer, Feature> featureMap);


    // Tiles

    default PlacedTile getPlacedTile(Position pos) {
        return getPlacedTiles().get(pos).getOrNull();
    }

    default PlacedTile getLastPlaced() {
        return getPlacedTiles().takeRight(1).map(Tuple2::_2).getOrNull();
    }

    default Stream<Tuple2<Location, PlacedTile>> getAdjacentTiles(Position pos) {
        return Stream.ofAll(Position.ADJACENT)
            .map(locPos-> locPos.map2(
                offset -> getPlacedTile(pos.add(offset))
            ))
            .filter(locTile -> locTile._2 != null);
    }

    default  Stream<Tuple2<Location, PlacedTile>> getAdjacentAndDiagonalTiles(Position pos) {
        return Stream.ofAll(Position.ADJACENT_AND_DIAGONAL)
            .map(locPos-> locPos.map2(
                offset -> getPlacedTile(pos.add(offset))
            ))
            .filter(locTile -> locTile._2 != null);
    }

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

    default Stream<Tuple2<Location, Feature>> getTileFeatures2(Position pos) {
        PlacedTile placedTile = getPlacedTile(pos);
        Rotation rot = placedTile.getRotation();
        Map<FeaturePointer, Feature> allFeatures = getFeatureMap();
        return Stream.ofAll(placedTile.getTile().getInitialFeatures())
            .map(t -> t.update1(t._1.rotateCW(rot)))
            .map(t -> t.update2(
                allFeatures.get(new FeaturePointer(pos, t._1)).get()
            ));
    }

    default <T extends Feature> Stream<Tuple2<Location, T>> getTileFeatures2(Position pos, Class<T> cls) {
        return Stream.narrow(getTileFeatures2(pos).filter(t -> cls.isInstance(t._2)));
    }

    default Feature getFeature(FeaturePointer fp) {
        if (fp.getLocation() == Location.ABBOT) fp = fp.setLocation(Location.CLOISTER);
        return getFeatureMap().get(fp).getOrNull();
    }

    default Feature getFeaturePartOf(FeaturePointer fp) {
        FeaturePointer normFp = fp.getLocation() == Location.ABBOT ? fp.setLocation(Location.CLOISTER) : fp;
        return getFeatureMap()
            .find(t -> normFp.isPartOf(t._1))
            .map(Tuple2::_2)
            .getOrNull();
    }
}
