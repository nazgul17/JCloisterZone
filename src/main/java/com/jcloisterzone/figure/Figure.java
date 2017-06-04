package com.jcloisterzone.figure;

import java.io.Serializable;
import java.util.Objects;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Game;

public abstract class Figure implements Serializable {

    private static final long serialVersionUID = 3264248810294656662L;

    protected transient final Game game;

    public Figure(Game game) {
        assert game != null;
        this.game = game;
    }

    public abstract void deploy(FeaturePointer at);
    public abstract void undeploy();

    public abstract FeaturePointer getFeaturePointer();

    public Feature getFeature() {
        FeaturePointer fp = getFeaturePointer();
        return fp == null ? null : game.getBoard().get(fp);
    }

    public Position getPosition() {
        FeaturePointer fp = getFeaturePointer();
        return fp == null ? null : fp.getPosition();
    }
    public Location getLocation() {
        FeaturePointer fp = getFeaturePointer();
        return fp == null ? null : fp.getLocation();
    }

    public boolean at(Position p) {
        return Objects.equals(p, getPosition());
    }

    public boolean at(FeaturePointer fp) {
        return Objects.equals(fp, getFeaturePointer());
    }

    public abstract boolean at(Feature feature);

    /** true if meeple is deployed on board */
    public boolean isDeployed() {
        return getFeaturePointer() != null;
    }

    //deployed is not opposite of supply, mind imprisoned followers
    public boolean isInSupply() {
        return getFeaturePointer() == null;
    }

    @Override
    public String toString() {
        FeaturePointer fp = getFeaturePointer();
        if (fp == null) {
            return getClass().getSimpleName();
        } else {
            return getClass().getSimpleName() + fp.toString();
        }
    }
}
