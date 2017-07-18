package com.jcloisterzone.reducers;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.MeepleReturned;
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
        state = state.appendEvent(
            new MeepleReturned(state.getActivePlayer(), meeple, source)
        );
        return state;

        //TODO check lonely Pig / Builder / Fairy

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
