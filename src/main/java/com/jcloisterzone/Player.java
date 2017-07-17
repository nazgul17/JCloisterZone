package com.jcloisterzone;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Objects;

import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.capability.ClothWineGrainCapability;
import com.jcloisterzone.ui.PlayerColor;

import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;

@Immutable
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    final private String nick;
    final private int index;
    final private PlayerSlot slot;

    public Player(String nick, int index, PlayerSlot slot) {
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
        if (this == o) return true;
        if (o instanceof Player) {
            return index == ((Player)o).index;
        }
        return false;
    }

    public Player getNextPlayer(GameState state) {
        int nextPlayerIndex = index == (state.getPlayers().length() - 1) ? 0 : index + 1;
        return state.getPlayers().get(nextPlayerIndex);
    }

    public Player getPrevPlayer(GameState state) {
        int prevPlayerIndex = index == 0 ? state.getPlayers().length() - 1 : index - 1;
        return state.getPlayers().get(prevPlayerIndex);
    }

    public int getPoints(GameState state) {
        return state.getScore().get(index).getPoints();
    }

    public PlayerClock getClock(GameState state) {
        return state.getClocks().get(index);
    }

    public int getPointsInCategory(GameState state, PointCategory cat) {
        HashMap<PointCategory, Integer> pointStats = state.getScore().get(getIndex()).getStats();
        Option<Integer> points = pointStats.get(cat);
        return points.getOrElse(0);
    }

    public Seq<Follower> getFollowers(GameState state) {
        return state.getFollowers().get(index);
    }

    public Seq<Special> getSpecialMeeples(GameState state) {
        return state.getSpecialMeeples().get(index);
    }

    public Stream<Meeple> getMeeples(GameState state) {
        return Stream.concat(getFollowers(state), getSpecialMeeples(state));
    }

    public boolean hasSpecialMeeple(GameState state, Class<? extends Special> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        return !Stream.ofAll(getSpecialMeeples(state))
            .filter(m -> m.getClass().equals(clazz))
            .filter(m -> m.isInSupply(state))
            .isEmpty();
    }

    public boolean hasFollower(GameState state) {
        return !Stream.ofAll(getFollowers(state))
            .filter(m -> m.isInSupply(state))
            .isEmpty();
    }

    public boolean hasFollower(GameState state, Class<? extends Follower> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        //check equality not instanceOf - phantom is subclass of small follower
        return !Stream.ofAll(getFollowers(state))
                .filter(m -> m.getClass().equals(clazz))
                .filter(m -> m.isInSupply(state))
                .isEmpty();
    }

    public Meeple getMeepleFromSupply(GameState state, Class<? extends Meeple> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        Seq<? extends Meeple> collection = (Follower.class.isAssignableFrom(clazz) ? getFollowers(state) : getSpecialMeeples(state));
        return Stream.ofAll(collection)
            .filter(m -> m.getClass().equals(clazz))
            .find(m -> m.isInSupply(state))
            .getOrNull();
    }

    public int getTradeResources(GameState state, TradeResource res) {
        return state
            .getCapability(ClothWineGrainCapability.class)
            .getTradeResources().get(index).get(res).getOrElse(0);
    }

    @Override
    public String toString() {
        return nick;
    }
}
