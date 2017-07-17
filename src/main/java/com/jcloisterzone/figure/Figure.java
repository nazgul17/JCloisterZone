package com.jcloisterzone.figure;

import java.io.Serializable;
import java.util.Objects;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.GameState;

@Immutable
public abstract class Figure<T extends BoardPointer> implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract T getDeployment(GameState state);

    public Feature getFeature(GameState state) {
        T at = getDeployment(state);
        FeaturePointer fp = at == null ? null : at.asFeaturePointer();
        if (fp == null) {
            return null;
        }
        return state.getFeatures().get(fp).getOrNull();
    }

    public Location getLocation(GameState state) {
        T at = getDeployment(state);
        FeaturePointer fp = at == null ? null : at.asFeaturePointer();
        return fp == null ? null : fp.getLocation();
    }

    public Position getPosition(GameState state) {
        T at = getDeployment(state);
        return at == null ? null : at.getPosition();
    }

    public boolean at(GameState state, Position p) {
        return Objects.equals(p, getPosition(state));
    }

    public boolean at(GameState state, FeaturePointer fp) {
        T at = getDeployment(state);
        return Objects.equals(fp, at == null ? null : at.asFeaturePointer());
    }

    public abstract boolean at(GameState state, Feature feature);

    /** true if meeple is deployed on board */
    public boolean isDeployed(GameState state) {
        return getDeployment(state) != null;
    }

    //deployed is not opposite of supply, mind imprisoned followers
    public boolean isInSupply(GameState state) {
        return getDeployment(state) == null;
    }
}
