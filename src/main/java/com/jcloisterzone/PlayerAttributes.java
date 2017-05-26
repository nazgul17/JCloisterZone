package com.jcloisterzone;

import java.util.Objects;

import com.jcloisterzone.game.PlayerSlot;

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

    @Override
    public int hashCode() {
        return Objects.hash(index, nick);
    }
}
