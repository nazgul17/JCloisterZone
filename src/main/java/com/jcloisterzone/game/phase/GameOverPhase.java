package com.jcloisterzone.game.phase;

import com.jcloisterzone.event.GameOverEvent;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.FinalScoring;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;


public class GameOverPhase extends Phase {

    public GameOverPhase(GameController gc) {
        super(gc);
    }

    @Override
    public void enter(GameState state) {
        if (isLocalPlayer(state.getTurnPlayer())) {
            //invoke only by single client
            getConnection().send(new ToggleClockMessage(game.getGameId(), null));
            getConnection().send(new GameOverMessage(game.getGameId()));
        }

        state = state.setPlayerActions(null);
        state = (new FinalScoring()).apply(state);
        promote(state);

        //TODO trigger event from game object ?
        game.post(new GameOverEvent());
    }
}
