package com.jcloisterzone.game.phase;

import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.reducers.FinalScoring;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;


public class GameOverPhase extends ServerAwarePhase {

    public GameOverPhase(Game game, GameController controller) {
        super(game, controller);
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

        game.post(new GameStateChangeEvent(GameStateChangeEvent.GAME_OVER));
    }
}
