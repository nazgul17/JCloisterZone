package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.function.Function;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.PrincessAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.FlierRollEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.GameState.Flag;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.FlierCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.capability.PortalCapability;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import com.jcloisterzone.reducers.UndeployMeeple;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.DeployFlierMessage;
import com.jcloisterzone.wsio.message.PassMessage;

import io.vavr.Function1;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;


public class ActionPhase extends Phase {

    private final TowerCapability towerCap;
    private final FlierCapability flierCap;
    private final PrincessCapability princessCapability;

    public ActionPhase(Game game) {
        super(game);
        towerCap = game.getCapability(TowerCapability.class);
        flierCap = game.getCapability(FlierCapability.class);
        princessCapability = game.getCapability(PrincessCapability.class);
    }

    private Stream<Tuple2<Location, Scoreable>> excludePrincess(Tile currentTile, Stream<Tuple2<Location, Scoreable>> s) {
        return s.filter(t -> {
            if (t._2 instanceof City) {
                City part = (City) currentTile.getInitialFeaturePartOf(t._1);
                return !part.isPrincess();
            } else {
                return true;
            }
        });
    }

    @Override
    public void enter(GameState state) {
        Player player = state.getTurnPlayer();

        Vector<Meeple> availMeeples = Vector
            .of(SmallFollower.class, BigFollower.class, Phantom.class, Builder.class, Pig.class)
            .map(cls -> player.getMeepleFromSupply(state, cls))
            .filter(Predicates.isNotNull());

        Tile currentTile = state.getBoard().getLastPlaced();
        Position currentTilePos = currentTile.getPosition();
        Stream<Tile> tiles;

        if (currentTile.hasTrigger(TileTrigger.PORTAL) && !state.getFlags().contains(Flag.PORTAL_USED)) {
            tiles = state.getBoard().getPlacedTiles();
        } else {
            tiles = Stream.of(currentTile);
        }

        Stream<Tuple2<FeaturePointer, Scoreable>> placesFp = tiles.flatMap(tile -> {
            Position pos = tile.getPosition();
            boolean isCurrentTile = pos.equals(currentTilePos);

            boolean placementAllowed = true;
            for (Capability cap : state.getCapabilities().values()) {
                if (!cap.isDeployAllowed(state, pos)) {
                    placementAllowed = false;
                    break;
                }
            }

            Stream<Tuple2<Location, Scoreable>> places;

            if (placementAllowed) {
                places = tile.getScoreables(!isCurrentTile);
                if (isCurrentTile && game.hasCapability(PrincessCapability.class) && state.getBooleanValue(CustomRule.PRINCESS_MUST_REMOVE_KNIGHT)) {
                    places = excludePrincess(tile, places);
                }
            } else {
                places = Stream.empty();
            }

            return places.map(t -> t.map1(loc -> new FeaturePointer(pos, loc)));
        });

        Vector<PlayerAction<?>> actions = availMeeples.map(meeple -> {
            Set<FeaturePointer> locations = placesFp
                .filter(t -> meeple.isDeploymentAllowed(state, t._1, t._2) == DeploymentCheckResult.OK)
                .map(t -> t._1)
                .toSet();

            PlayerAction<?> action = new MeepleAction(meeple.getClass(), locations);
            return action;
        });

        actions = actions.filter(action -> !action.isEmpty());

        GameState nextState = state.setPlayerActions(
            new ActionsState(player, actions, true)
        );

        for (Capability cap : nextState.getCapabilities().values()) {
            nextState = cap.onActionPhaseEntered(nextState);
        }

        promote(nextState);
    }

    private GameState clearActions(GameState state) {
        return state.setPlayerActions(null);
    }

    @Override
    public void notifyRansomPaid() {
        enter(); //recompute available actions
    }

    @WsSubscribe
    public void handlePass(PassMessage msg) {
        GameState state = game.getState();
        state = clearActions(state);
        if (getDefaultNext() instanceof PhantomPhase) {
            //skip PhantomPhase if user pass turn
            getDefaultNext().next(state);
        } else {
            next(state);
        }
    }

    @Override
    public void deployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType) {
        game.markUndo();
        GameState state = game.getState();
        Meeple m = state.getActivePlayer().getMeepleFromSupply(state, meepleType);
        //TODO nice to have validation in separate class (can be turned off eg for loadFromSnapshots or in AI (to speed it)
        if (m instanceof Follower) {
            if (state.getBoard().get(fp).isOccupied(state)) {
                throw new IllegalArgumentException("Feature is occupied.");
            }
        }
        Tile tile = state.getBoard().getLastPlaced();

        state = (new DeployMeeple(m, fp)).apply(state);

        if (fp.getLocation() != Location.TOWER && tile.hasTrigger(TileTrigger.PORTAL) && !fp.getPosition().equals(tile.getPosition())) {
            state = state.addFlag(Flag.PORTAL_USED);
        }
        state = clearActions(state);
        next(state);
    }

    @Override
    public void moveNeutralFigure(BoardPointer ptr, Class<? extends NeutralFigure> figureType) {
        GameState state = game.getState();
        if (Fairy.class.equals(figureType)) {
            // TODO IMMUTABLE validation against ActionState
//            if (!Iterables.any(getActivePlayer().getFollowers(), MeeplePredicates.at(ptr.getPosition()))) {
//                throw new IllegalArgumentException("The tile has deployed not own follower.");
//            }
            assert (state.getBooleanValue(CustomRule.FAIRY_ON_TILE) ? Position.class : BoardPointer.class).isInstance(ptr);

            Fairy fairy = state.getNeutralFigures().getFairy();
            state = (new MoveNeutralFigure<BoardPointer>(fairy, ptr, state.getActivePlayer())).apply(state);
            state = clearActions(state);
            next(state);
            return;
        }
        super.moveNeutralFigure(ptr, figureType);
    }

    @Override
    public void undeployMeeple(MeeplePointer ptr) {
        //TOOD use different messages for undeploy actions?
        GameState state = game.getState();

        Meeple meeple = state.getDeployedMeeples().find(m -> ptr.match(m._1)).map(t -> t._1)
            .getOrElseThrow(() -> new IllegalArgumentException("Pointer doesn't match any meeple"));

        PrincessAction princessAction = (PrincessAction) state.getPlayerActions().getActions().find(Predicates.instanceOf(PrincessAction.class)).getOrNull();
        if (princessAction != null) {
            if (princessAction.getOptions().contains(ptr)) {
                state = state.addFlag(Flag.PRINCESS_USED);
            } else {
                throw new IllegalArgumentException("Pointer doesn't match princess action");
            }
        } else {
            throw new IllegalArgumentException("Undeploy is not allowed");
        }

        state = (new UndeployMeeple(meeple)).apply(state);
        state = clearActions(state);
        next(state);
    }


    @Override
    public void placeTowerPiece(Position p) {
        GameState state = game.getState();
        //TODO
        towerCap.placeTowerPiece(getActivePlayer(), p);

        state = clearActions(state);
        next(state, TowerCapturePhase.class);
    }

    @Override
    public void placeLittleBuilding(LittleBuilding lbType) {
        GameState state = game.getState();
        //TODO
        LittleBuildingsCapability lbCap = game.getCapability(LittleBuildingsCapability.class);
        lbCap.placeLittleBuilding(getActivePlayer(), lbType);

        state = clearActions(state);
        next(state);
    }


    private boolean isFestivalUndeploy(Meeple m) {
        return getTile().hasTrigger(TileTrigger.FESTIVAL) && m.getPlayer() == getActivePlayer();
    }

    private boolean isPrincessUndeploy(Meeple m) {
        boolean tileHasPrincess = false;
        for (Feature f : getTile().getFeatures()) {
            if (f instanceof City) {
                City c = (City) f;
                if (c.isPricenss()) {
                    tileHasPrincess = true;
                    break;
                }
            }
        }
        //check if it is same city should be here to be make exact check
        return tileHasPrincess && m.getFeature() instanceof City;
    }

    @Override
    public void undeployMeeple(MeeplePointer mp) {
        Meeple m = game.getMeeple(mp);
        boolean princess = isPrincessUndeploy(m);
        if (isFestivalUndeploy(m) || princess) {
            m.undeploy();
            if (princess) {
                princessCapability.setPrincessUsed(true);
            }
            //TODO skip PhantomPhase there!!!
            next();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void placeTunnelPiece(FeaturePointer fp, boolean isB) {
        game.getCapability(TunnelCapability.class).placeTunnelPiece(fp, isB);
        next(ActionPhase.class);
    }


    @Override
    public void deployBridge(Position pos, Location loc) {
        BridgeCapability bridgeCap = game.getCapability(BridgeCapability.class);
        bridgeCap.decreaseBridges(getActivePlayer());
        bridgeCap.deployBridge(pos, loc, false);
        next(ActionPhase.class);
    }

    @WsSubscribe
    public void handleDeployFlier(DeployFlierMessage msg) {
        game.updateRandomSeed(msg.getCurrentTime());
        int distance = game.getRandom().nextInt(3) + 1;
        flierCap.setFlierUsed(true);
        flierCap.setFlierDistance(msg.getMeepleTypeClass(), distance);
        game.post(new FlierRollEvent(getActivePlayer(), getTile().getPosition(), distance));
        next(FlierActionPhase.class);
    }
}
