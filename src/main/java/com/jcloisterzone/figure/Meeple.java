package com.jcloisterzone.figure;

import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Game;

import io.vavr.Predicates;
import io.vavr.collection.LinkedHashMap;

public abstract class Meeple extends Figure<FeaturePointer> {

    private static final long serialVersionUID = 251811435063355665L;

    private final String id;

    private transient final PlayerAttributes player;


    public Meeple(Game game, Integer idSuffix, PlayerAttributes player) {
        super(game);
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
    public FeaturePointer getDeployment() {
        return game.getDeployedMeeples().get(this).getOrNull();
    }

    @Override
    public boolean at(Feature feature) {
        return feature.getMeeples().find(Predicates.is(this)).isDefined();
    }

    public boolean at(MeeplePointer mp) {
        if (!at(mp.asFeaturePointer())) return false;
        if (!mp.getMeepleId().equals(id)) return false;
        return true;
    }

    public boolean canBeEatenByDragon() {
        return true;
    }

    public DeploymentCheckResult isDeploymentAllowed(Feature feature) {
        return DeploymentCheckResult.OK;
    }

    @Override
    public void deploy(FeaturePointer at) {
        FeaturePointer origin = getDeployment();
        DeploymentCheckResult check = isDeploymentAllowed(game.getBoard().get(at));
        if (!check.result) {
          throw new IllegalArgumentException(check.error);
        }
        game.replaceState(state -> {
            LinkedHashMap<Meeple, FeaturePointer> deployedMeeples = state.getDeployedMeeples();
            return state.setDeployedMeeples(deployedMeeples.put(this, at));
        });
        game.post(new MeepleEvent(game.getActivePlayer(), this, origin, at));
    }

    @Override
    public void undeploy() {
        undeploy(true);
    }

    public void undeploy(boolean checkForLonelyBuilderOrPig) {
        FeaturePointer source = getDeployment();
        assert source != null;
        game.replaceState(state -> {
            LinkedHashMap<Meeple, FeaturePointer> deployedMeeples = state.getDeployedMeeples();
            return state.setDeployedMeeples(deployedMeeples.remove(this));
        });
        game.post(new MeepleEvent(game.getActivePlayer(), this, source, null));
    }


    public PlayerAttributes getPlayer() {
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
