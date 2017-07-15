package com.jcloisterzone.game.phase;

import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.reducers.FinalScoring;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;


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

        game.replaceState(new FinalScoring());
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
