package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.event.play.PlayerTurnEvent;

public class SetNextPlayer implements Reducer {

    @Override
    public GameState apply(GameState state) {
        Player p = state.getTurnPlayer().getNextPlayer(state);
        state = state.mapPlayers(ps -> ps.setTurnPlayerIndex(p.getIndex()));
        state = state.appendEvent(
            new PlayerTurnEvent(PlayEventMeta.createWithoutPlayer(), p)
        );
        return state;
    }

}
