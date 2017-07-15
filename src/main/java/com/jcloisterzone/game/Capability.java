package com.jcloisterzone.game;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.PlayEvent;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;

import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;


public abstract class Capability {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());


//    protected TileDefinition getCurrentTile() {
//        return game.getCurrentTile();
//    }

    /* no @Subscribe for Capabilities
     * it cause post from another event handler and makes trouble with AI tasks
     * */
    public void handleEvent(PlayEvent event) {
    }

    @Deprecated
    public void saveToSnapshot(Document doc, Element node) {
    }

    @Deprecated
    public void saveTileToSnapshot(Tile tile, Document doc, Element tileNode) {
    }

    @Deprecated
    public void loadFromSnapshot(Document doc, Element node) throws SnapshotCorruptedException {
    }

    @Deprecated
    public void loadTileFromSnapshot(Tile tile, Element tileNode) {
    }

    public TileDefinition initTile(TileDefinition tile, Element xml) {
        return tile;
    }

    public Feature initFeature(String tileId, Feature feature, Element xml) {
        return feature;
    }

    public List<Feature> extendFeatures(String tileId) {
        return List.empty();
    }

    public String getTileGroup(TileDefinition tile) {
        return null;
    }

    public List<Follower> createPlayerFollowers(Player p) {
        return List.empty();
    }

    public List<Special> createPlayerSpecialMeeples(Player p) {
        return List.empty();
    }

    public void begin() {
    }

    /** convenient method to find follower action in all actions */
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

    public Set<FeaturePointer> extendFollowOptions(Set<FeaturePointer> locations) {
        return locations;
    }

    public Vector<PlayerAction<?>> prepareActions(Vector<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        return actions;
    }

    public Vector<PlayerAction<?>> postPrepareActions(Vector<PlayerAction<?>> actions) {
        return actions;
    }

    //TODO don't use Tile ? FeaturePointer shoudl be used
    public boolean isDeployAllowed(Tile tile, Class<? extends Meeple> meepleType) {
        return true;
    }

    //NICE TO HAVE RENAME it - hook invoked before scoring is made on completed completable
    public void scoreCompleted(Completable feature) {
    }

    public void turnCleanUp() {
    }

    public void turnPartCleanUp() {
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
