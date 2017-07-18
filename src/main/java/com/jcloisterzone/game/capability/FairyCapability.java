package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameState;

@Immutable
public class FairyCapability extends Capability {

    private static final long serialVersionUID = 1L;

    public static final int FAIRY_POINTS_BEGINNING_OF_TURN = 1;
    public static final int FAIRY_POINTS_FINISHED_OBJECT = 3;

    @Override
    public GameState onStartGame(GameState state) {
        return state.setNeutralFigures(
            state.getNeutralFigures().setFairy(new Fairy())
        );
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
//
//    public Fairy getFairy() {
//        return fairy;
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
