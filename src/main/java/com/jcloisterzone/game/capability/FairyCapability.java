package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.FairyNextToAction;
import com.jcloisterzone.action.FairyOnTileAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.GameState;

import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Set;

@Immutable
public class FairyCapability extends Capability {

    private static final long serialVersionUID = 1L;

    public static final int FAIRY_POINTS_BEGINNING_OF_TURN = 1;
    public static final int FAIRY_POINTS_FINISHED_OBJECT = 3;

    @Override
    public GameState onStartGame(GameState state) {
        return state.setNeutralFigures(
            state.getNeutralFigures().setFairy(new Fairy("fairy.1"))
        );
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        boolean fairyOnTile = state.getBooleanValue(CustomRule.FAIRY_ON_TILE);
        Player activePlayer = state.getPlayerActions().getPlayer();
        PlayerAction<?> fairyAction = null;

//        if (fairyOnTile) {
//            fairyAction = new FairyOnTileAction();
//        } else {
//            fairyAction = new FairyNextToAction();
//        }

        LinkedHashMap<Follower, FeaturePointer> followers = LinkedHashMap.narrow(
            state.getDeployedMeeples()
                .filter((m, fp) -> (m instanceof Follower) && m.getPlayer().equals(activePlayer))
        );

        if (fairyOnTile) {
            Set<Position> options = followers.values().map(fp -> fp.getPosition()).toSet();
            if (!options.isEmpty()) {
                fairyAction = new FairyOnTileAction(options);
            }
        } else {
            Set<MeeplePointer> options = followers
                .map(t -> new MeeplePointer(t._2, t._1.getId()))
                .toSet();
            if (!options.isEmpty()) {
                fairyAction = new FairyNextToAction(options);
            }
        }

        if (fairyAction == null) {
            return state;
        }

        ActionsState as = state.getPlayerActions();
        return state.setPlayerActions(as.appendAction(fairyAction));
    }

//    @Override
//    public void handleEvent(PlayEvent event) {
//       if (event instanceof MeepleEvent) {
//           undeployed((MeepleEvent) event);
//       }
//
//    }
//
//    private void undeployed(MeepleEvent ev) {
//        if (ev.getFrom() == null) return;
//        if (ev.getMeeple() == fairy.getNextTo()) {
//            fairy.setNextTo(null);
//        }
//    }


//    public boolean isNextTo(Follower f) {
//        if (game.getBooleanValue(CustomRule.FAIRY_ON_TILE)) {
//            Position pos = f.getPosition();
//            return pos != null && pos.equals(fairy.getPosition());
//        } else {
//            return fairy.getNextTo() == f && f.at(fairy.getFeaturePointer());
//        }
//    }

//    public List<Follower> getFollowersNextToFairy() {
//        if (fairy.getFeaturePointer() == null) {
//            return Collections.emptyList();
//        }
//        List<Follower> result = new ArrayList<>();
//        for (Meeple m : game.getDeployedMeeples()) {
//            if (m instanceof Follower) {
//                Follower f = (Follower) m;
//                if (isNextTo(f)) {
//                    result.add(f);
//                }
//            }
//        }
//        return result;
//    }
//
//
//    @Override
//    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
//        boolean fairyOnTile = game.getBooleanValue(CustomRule.FAIRY_ON_TILE);
//        Player activePlayer = game.getActivePlayer();
//        PlayerAction<?> fairyAction;
//        if (fairyOnTile) {
//            fairyAction = new FairyOnTileAction();
//        } else {
//            fairyAction = new FairyNextToAction();
//        }
//
//        for (Follower m : Iterables.filter(activePlayer.getFollowers(), MeeplePredicates.deployed())) {
//            if (fairyOnTile) {
//                if (!m.at(fairy.getPosition())) {
//                    ((FairyOnTileAction) fairyAction).add(m.getPosition());
//                }
//            } else {
//                if (!m.equals(fairy.getNextTo())) {
//                    ((FairyNextToAction) fairyAction).add(new MeeplePointer(m));
//                }
//            }
//        }
//
//        if (!fairyAction.isEmpty()) {
//            actions.add(fairyAction);
//        }
//    }

}
