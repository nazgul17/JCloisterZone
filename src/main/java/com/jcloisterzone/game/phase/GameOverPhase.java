package com.jcloisterzone.game.phase;

import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.reducers.ScoreFeature;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;

import io.vavr.Predicates;
import io.vavr.collection.Stream;


public class GameOverPhase extends ServerAwarePhase {

    public GameOverPhase(Game game, GameController controller) {
        super(game, controller);
    }

    @Override
    public void enter() {
        if (isLocalPlayer(game.getTurnPlayer())) {
            //invoke only by single client
            getConnection().send(new ToggleClockMessage(game.getGameId(), null));
            getConnection().send(new GameOverMessage(game.getGameId()));
        }

        GameState state = game.getState();

        Stream<Scoreable> scoreables = state.getBoard().getOccupiedScoreables();

        //score first all except farms
        for (Scoreable feature : scoreables.filter(Predicates.<Scoreable>instanceOf(Farm.class).negate())) {
            state = (new ScoreFeature(feature)).apply(state);
        }

        //then score farms
        for (Scoreable feature : scoreables.filter(Predicates.instanceOf(Farm.class))) {
            state = (new ScoreFeature(feature)).apply(state);

            //IMMUTABLE TODO solve Barn scoring
        }

        game.replaceState(state);
        game.finalScoring();
        game.post(new GameStateChangeEvent(GameStateChangeEvent.GAME_OVER));
    }

//    @Override
//    public void scoreBarn(FarmScoreContext ctx, Barn meeple) {
//        int points = ctx.getBarnPoints();
//        meeple.getPlayer().addPoints(points, PointCategory.FARM);
//        ScoreEvent ev = new ScoreEvent(meeple.getFeature(), points, PointCategory.FARM, meeple);
//        ev.setFinal(true);
//        game.post(ev);
//    }

}
