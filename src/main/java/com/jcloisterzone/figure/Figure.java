package com.jcloisterzone.figure;

import java.io.Serializable;
import java.util.Objects;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Game;

public abstract class Figure<T extends BoardPointer> implements Serializable {

    private static final long serialVersionUID = 3264248810294656662L;

    protected transient final Game game;

    public Figure(Game game) {
        assert game != null;
        this.game = game;
    }

    public abstract void deploy(T at);
    public abstract void undeploy();

    public abstract T getDeployment();

    public Feature getFeature() {
        T at = getDeployment();
        FeaturePointer fp = at == null ? null : at.asFeaturePointer();
        return fp == null ? null : game.getBoard().get(fp);
    }

    public Location getLocation() {
        T at = getDeployment();
        FeaturePointer fp = at == null ? null : at.asFeaturePointer();
        return fp == null ? null : fp.getLocation();
    }

    public Position getPosition() {
        T at = getDeployment();
        return at == null ? null : at.getPosition();
    }


    public boolean at(Position p) {
        return Objects.equals(p, getPosition());
    }

    public boolean at(FeaturePointer fp) {
        T at = getDeployment();
        return Objects.equals(fp, at == null ? null : at.asFeaturePointer());
    }

    public abstract boolean at(Feature feature);

    /** true if meeple is deployed on board */
    public boolean isDeployed() {
        return getDeployment() != null;
    }

    //deployed is not opposite of supply, mind imprisoned followers
    public boolean isInSupply() {
        return getDeployment() == null;
    }

    @Override
    public String toString() {
        T at = getDeployment();
        if (at == null) {
            return getClass().getSimpleName();
        } else {
            return getClass().getSimpleName() + at.toString();
        }
    }
}
