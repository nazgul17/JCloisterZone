package com.jcloisterzone.game;

import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;

import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

@Immutable
public abstract class Capability implements Serializable {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());


    public TileDefinition initTile(TileDefinition tile, Element xml) {
        return tile;
    }

    public Feature initFeature(GameSettings settings, String tileId, Feature feature, Element xml) {
        return feature;
    }

    public List<Feature> extendFeatures(String tileId) {
        return List.empty();
    }

    public String getTileGroup(TileDefinition tile) {
        return null;
    }

    public List<Follower> createPlayerFollowers(Player player) {
        return List.empty();
    }

    public List<Special> createPlayerSpecialMeeples(Player player) {
        return List.empty();
    }

    /** convenient method to find follower action in all actions */
    @Deprecated
    protected java.util.List<MeepleAction> findFollowerActions(java.util.List<PlayerAction<?>> actions) {
        java.util.List<MeepleAction> followerActions = new ArrayList<>();
        for (PlayerAction<?> a : actions) {
            if (a instanceof MeepleAction) {
                MeepleAction ma = (MeepleAction) a;
                if (Follower.class.isAssignableFrom(ma.getMeepleType())) {
                    followerActions.add(ma);
                }
            }
        }
        return followerActions;
    }

    /** convenient method to find follower action in all actions, or create new if player has follower and action doesn't exists*/
    @Deprecated
    protected java.util.List<MeepleAction> findAndFillFollowerActions(java.util.List<PlayerAction<?>> actions) {
        java.util.List<MeepleAction> followerActions = findFollowerActions(actions);
        java.util.Set<Class<? extends Meeple>> hasAction = new java.util.HashSet<>();
        for (MeepleAction ma : followerActions) {
            hasAction.add(ma.getMeepleType());
        }

        for (Follower f : game.getActivePlayer().getFollowers()) {
            if (f.isInSupply() && !hasAction.contains(f.getClass())) {
                MeepleAction ma = new MeepleAction(f.getClass());
                actions.add(ma);
                followerActions.add(ma);
                hasAction.add(f.getClass());
            }
        }
        return followerActions;
    }

    @Deprecated
    public Set<FeaturePointer> extendFollowOptions(Set<FeaturePointer> locations) {
        return locations;
    }

//    @Deprecated
//    public Vector<PlayerAction<?>> prepareActions(Vector<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
//        return actions;
//    }
//
//    @Deprecated
//    public Vector<PlayerAction<?>> postPrepareActions(Vector<PlayerAction<?>> actions) {
//        return actions;
//    }

    public boolean isDeployAllowed(GameState state, Position pos) {
        return true;
    }

    public GameState onStartGame(GameState state) {
        return state;
    }

    public GameState onTilePlaced(GameState state) {
        return state;
    }

    public GameState onCompleted(GameState state, Completable feature) {
        return state;
    }

    public GameState onActionPhaseEntered(GameState state) {
        return state;
    }

    public GameState turnCleanUp(GameState state) {
        return state;
    }

    public GameState turnPartCleanUp(GameState state) {
        return state;
    }

    public GameState finalScoring(GameState state) {
        return state;
    }

    public boolean isTilePlacementAllowed(TileDefinition tile, Position p) {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().replace("Capability", "");
    }

}
