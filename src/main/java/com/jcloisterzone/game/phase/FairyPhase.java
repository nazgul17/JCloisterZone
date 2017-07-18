package com.jcloisterzone.game.phase;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.reducers.AddPoints;

import io.vavr.Tuple2;


public class FairyPhase extends Phase {

    public FairyPhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(FairyCapability.class);
    }

    @Override
    public void enter(GameState state) {
        BoardPointer ptr = state.getNeutralFigureDeployment(Fairy.class);
        if (ptr == null) {
            next(state);
            return;
        }

        FeaturePointer fairyFp = ptr.asFeaturePointer();
        boolean onTileRule = game.getBooleanValue(CustomRule.FAIRY_ON_TILE);
        for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
            Meeple m = t._1;
            if (!m.getPlayer().equals(state.getTurnPlayer())) continue;
            if (!t._2.equals(fairyFp)) continue;

            if (!onTileRule) {
                if (!((MeeplePointer) ptr).getMeepleId().equals(m.getId())) continue;
            }

            state = new AddPoints(
                m.getPlayer(),
                FairyCapability.FAIRY_POINTS_BEGINNING_OF_TURN,
                PointCategory.FAIRY
            ).apply(state);
        }

        next(state);
    }
}
