package com.jcloisterzone;

import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.ui.PlayerColor;

// is it needed
public interface IPlayer {

    String getNick();
    int getIndex();
    PlayerSlot getSlot();

    PlayerColor getColors();
    boolean isLocalHuman();
}
