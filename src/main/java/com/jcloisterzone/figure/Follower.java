package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.RemoveLonelyBuilderAndPig;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.PigCapability;

public abstract class Follower extends Meeple {

    private static final long serialVersionUID = -659337195197201811L;

    //private boolean inPrison;

    public Follower(Game game, Integer idSuffix, PlayerAttributes player) {
        super(game, idSuffix, player);
    }

    public int getPower() {
        return 1;
    }

    @Override
    public boolean canBeEatenByDragon() {
        return !(getFeature() instanceof Castle);
    }

    public boolean isInPrison() {
        //IMMUTABLE TOOD
        //return inPrison;
        return false;
    }

    @Override
    public boolean isInSupply() {
        return !isInPrison() && super.isInSupply();
    }


    //TODO ??? can be this in score visitor instead of here ???
    @Override
    public void undeploy(boolean checkForLonelyBuilderOrPig) {
        assert !isInPrison();
        //store reference which is lost by super call
        Feature f = getFeature();
        super.undeploy(checkForLonelyBuilderOrPig); //clear
        if (checkForLonelyBuilderOrPig) {
//            boolean builder = game.hasCapability(BuilderCapability.class) && (piece instanceof City || piece instanceof Road);
//            boolean pig = game.hasCapability(PigCapability.class) && piece instanceof Farm;
//            if (builder || pig) {
//                Special toRemove = piece.walk(new RemoveLonelyBuilderAndPig(getPlayer()));
//                if (toRemove != null) {
//                    toRemove.undeploy(false);
//                }
//            }
            //IMMUTABLE TODO
        }
    }

    @Override
    public String toString() {
        return super.toString() + (isInPrison() ? "(PRISON)" : "");
    }
}
