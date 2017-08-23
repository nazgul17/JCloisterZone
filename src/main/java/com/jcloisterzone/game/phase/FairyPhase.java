package com.jcloisterzone.game.phase;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.SiegeCapability;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.AddPoints;

import io.vavr.Tuple2;


@RequiredCapability(FairyCapability.class)
public class FairyPhase extends Phase {

    public FairyPhase(Game game) {
        super(game);
    }

    @Override
    public void enter(GameState state) {
        BoardPointer ptr = state.getNeutralFigures().getFairyDeployment();
        if (ptr == null) {
            next(state);
            return;
        }

        boolean onTileRule = ptr instanceof Position;
        FeaturePointer fairyFp = ptr.asFeaturePointer();

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

            state = state.appendEvent(new ScoreEvent(
                FairyCapability.FAIRY_POINTS_BEGINNING_OF_TURN, PointCategory.FAIRY,
                false, fairyFp, m
            ));
        }

        next(state);
    }
}
