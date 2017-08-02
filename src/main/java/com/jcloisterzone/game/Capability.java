package com.jcloisterzone.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.List;
import io.vavr.collection.Set;

@Immutable
public abstract class Capability<T> implements Serializable {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());


    @SuppressWarnings("unchecked")
    private Class<? extends Capability<T>> narrowClass() {
        return (Class<? extends Capability<T>>) getClass();
    }

    public final T getModel(GameState state) {
        return state.getCapabilities().getModel(narrowClass());
    }

    public final GameState updateModel(GameState state, Function<T, T> fn) {
        return state.updateCapabilityModel(narrowClass(), fn);
    }

    public final GameState setModel(GameState state, T model) {
        return state.setCapabilityModel(narrowClass(), model);
    }


    public TileDefinition initTile(TileDefinition tile, Element xml) {
        return tile;
    }

    //TODO use only settings state linked from whole state?
    public Feature initFeature(GameState settings, String tileId, Feature feature, Element xml) {
        return feature;
    }

//    public List<Feature> extendFeatures(String tileId) {
//        return List.empty();
//    }

    public String getTileGroup(TileDefinition tile) {
        return null;
    }

    public List<Follower> createPlayerFollowers(Player player, MeepleIdProvider idProvider) {
        return List.empty();
    }

    public List<Special> createPlayerSpecialMeeples(Player player, MeepleIdProvider idProvider) {
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

//    /** convenient method to find follower action in all actions, or create new if player has follower and action doesn't exists*/
//    @Deprecated
//    protected java.util.List<MeepleAction> findAndFillFollowerActions(java.util.List<PlayerAction<?>> actions) {
//        java.util.List<MeepleAction> followerActions = findFollowerActions(actions);
//        java.util.Set<Class<? extends Meeple>> hasAction = new java.util.HashSet<>();
//        for (MeepleAction ma : followerActions) {
//            hasAction.add(ma.getMeepleType());
//        }
//
//        for (Follower f : game.getActivePlayer().getFollowers()) {
//            if (f.isInSupply() && !hasAction.contains(f.getClass())) {
//                MeepleAction ma = new MeepleAction(f.getClass());
//                actions.add(ma);
//                followerActions.add(ma);
//                hasAction.add(f.getClass());
//            }
//        }
//        return followerActions;
//    }

    @Deprecated
    public Set<FeaturePointer> extendFollowOptions(Set<FeaturePointer> locations) {
        return locations;
    }

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

//    public GameState turnCleanUp(GameState state) {
//        return state;
//    }

    // deprecated ???
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
