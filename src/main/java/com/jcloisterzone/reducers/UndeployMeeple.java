package com.jcloisterzone.reducers;

import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.GameState;

import io.vavr.collection.LinkedHashMap;

public class UndeployMeeple implements Reducer {

    private final Meeple meeple;

    public UndeployMeeple(Meeple meeple) {
        this.meeple = meeple;
    }

    @Override
    public GameState apply(GameState state) {
        FeaturePointer source = meeple.getDeployment(state);
        assert source != null;

        LinkedHashMap<Meeple, FeaturePointer> deployedMeeples = state.getDeployedMeeples();
        state = state.setDeployedMeeples(deployedMeeples.remove(meeple));
        state = state.setEvents(state.getEvents().append(
            new MeepleEvent(state.getActivePlayer(), meeple, source, null)
        ));
        return state;

//         if (checkForLonelyBuilderOrPig) {
//            boolean builder = game.hasCapability(BuilderCapability.class) && (piece instanceof City || piece instanceof Road);
//            boolean pig = game.hasCapability(PigCapability.class) && piece instanceof Farm;
//            if (builder || pig) {
//                Special toRemove = piece.walk(new RemoveLonelyBuilderAndPig(getPlayer()));
//                if (toRemove != null) {
//                    toRemove.undeploy(false);
//                }
//            }
            //IMMUTABLE TODO
//        }
    }

}
