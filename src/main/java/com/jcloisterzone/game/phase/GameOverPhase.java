package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.Game;
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

        Stream<Scoreable> scoreables = Stream.ofAll(game.getBoard().getAllFeatures())
            .filter(Predicates.instanceOf(Scoreable.class))
            .filter(f -> f.isOccupied())
            .map(f -> (Scoreable) f);

        //score first all except farms
        for (Scoreable feature : scoreables.filter(Predicates.<Scoreable>instanceOf(Farm.class).negate())) {
            game.scoreFeature(feature);
        }

        //then score farms
        for (Scoreable feature : scoreables.filter(Predicates.instanceOf(Farm.class))) {
            game.scoreFeature(feature);

            //IMMUTABLE TODO solve Barn scoring
        }

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


    @Override
    public Player getActivePlayer() {
        return null;
    }


}
