package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.GameState;

import io.vavr.Predicates;

public abstract class Meeple extends Figure<FeaturePointer> {

    private static final long serialVersionUID = 251811435063355665L;

    private final String id;

    private transient final Player player;

    public Meeple(Integer idSuffix, Player player) {
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(player.getIndex());
        idBuilder.append(".");
        idBuilder.append(getClass().getSimpleName());
        if (idSuffix != null) {
            idBuilder.append(".");
            idBuilder.append(idSuffix.toString());
        }
        this.id = idBuilder.toString();
        this.player = player;
    }

    public String getId() {
        return id;
    }

    @Override
    public FeaturePointer getDeployment(GameState state) {
        return state.getDeployedMeeples().get(this).getOrNull();
    }

    @Override
    public boolean at(GameState state, Feature feature) {
        return feature.getMeeples(state).find(Predicates.is(this)).isDefined();
    }

    public boolean at(GameState state, MeeplePointer mp) {
        if (!at(state, mp.asFeaturePointer())) return false;
        if (!mp.getMeepleId().equals(id)) return false;
        return true;
    }

    public boolean canBeEatenByDragon(GameState state) {
        return true;
    }

    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Feature feature) {
        return DeploymentCheckResult.OK;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Meeple)) return false;
        return this == obj || id.equals(((Meeple)obj).id);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + player.getNick() + "," + id + ")";
    }
}
