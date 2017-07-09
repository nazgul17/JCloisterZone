package com.jcloisterzone.action;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.layer.FeatureAreaLayer;

import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public abstract class SelectFeatureAction extends PlayerAction<FeaturePointer> {

    public SelectFeatureAction(Set<FeaturePointer> options) {
        super(options);
    }

    @Override
    protected Class<? extends ActionLayer<?>> getActionLayerType() {
        return FeatureAreaLayer.class;
    }

    // TODO is map to Location needed
    // TODO is grouping needed at all?
    public Map<Position, Set<Location>> groupByPosition() {
        return getOptions()
            .groupBy(tp -> tp.getPosition())
            .mapValues(setOfPlacements -> setOfPlacements.map(tp -> tp.getLocation()));
    }

    //TODO direct implementation
    public Set<Location> getLocations(Position pos) {
        return groupByPosition().getOrElse(pos, HashSet.empty());
    }


}
