package com.jcloisterzone;

import java.util.Objects;

import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.ui.PlayerColor;

@Immutable
public class PlayerAttributes implements IPlayer {

    final private String nick;
    final private int index;
    final private PlayerSlot slot;

    public PlayerAttributes(String nick, int index, PlayerSlot slot) {
        this.nick = nick;
        this.index = index;
        this.slot = slot;
    }

    public String getNick() {
        return nick;
    }

    public int getIndex() {
        return index;
    }

    public PlayerSlot getSlot() {
        return slot;
    }

    public PlayerColor getColors() {
        return slot.getColors();
    }

    public boolean isLocalHuman() {
        return slot.isOwn() && !slot.isAi();
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, nick);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof IPlayer) {
            if (((IPlayer) o).getIndex() == getIndex() && getIndex() != -1)
                return true;
        }
        return false;
    }
}
