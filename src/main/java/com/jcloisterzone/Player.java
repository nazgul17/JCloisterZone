package com.jcloisterzone;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.immutable.GameState;
import com.jcloisterzone.immutable.Immutable;
import com.jcloisterzone.ui.PlayerColor;

import io.vavr.control.Option;


/**
 * Represents one player in game. Contains information about figures, points and
 * control informations.<br>
 *
 * @author Roman Krejcik
 */
public class Player implements Serializable {

    private static final long serialVersionUID = -7276471952562769832L;

    private final List<Follower> followers = new ArrayList<Follower>(SmallFollower.QUANTITY + 3);
    private final List<Special> specialMeeples = new ArrayList<Special>(3);
    private final Iterable<Meeple> meeples = Iterables.<Meeple>concat(followers, specialMeeples);

    final private String nick;
    final private int index;
    private PlayerSlot slot;
    private final PlayerClock clock = new PlayerClock();

    public Player(String nick, int index, PlayerSlot slot) {
        this.nick = nick;
        this.index = index;
        this.slot = slot;
    }

    public void addMeeple(Meeple meeple) {
        if (meeple instanceof Follower) {
            followers.add((Follower) meeple);
        } else {
            specialMeeples.add((Special) meeple);
        }
    }

    public List<Follower> getFollowers() {
        return followers;
    }

    public List<Special> getSpecialMeeples() {
        return specialMeeples;
    }

    public Iterable<Meeple> getMeeples() {
        return meeples;
    }

    public boolean hasSpecialMeeple(Class<? extends Special> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        return Iterables.any(specialMeeples, Predicates.and(MeeplePredicates.inSupply(), MeeplePredicates.type(clazz)));
    }

    public boolean hasFollower() {
        return Iterables.any(followers, MeeplePredicates.inSupply());
    }

    public boolean hasFollower(Class<? extends Follower> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        //chcek equality not instanceOf - phantom is subclass of small follower
        return Iterables.any(followers, Predicates.and(MeeplePredicates.inSupply(), MeeplePredicates.type(clazz)));
    }

    public Meeple getMeepleFromSupply(Class<? extends Meeple> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        Iterable<? extends Meeple> collection = (Follower.class.isAssignableFrom(clazz) ? followers : specialMeeples);
        return Iterables.find(collection, Predicates.and(MeeplePredicates.inSupply(), MeeplePredicates.type(clazz)));
    }

    public int getPoints(GameState state) {
        return state.getScore(this).getPoints();
    }

    public String getNick() {
        return nick;
    }

    @Override
    public String toString() {
        return nick;
    }

    public int getIndex() {
        return index;
    }

    public PlayerColor getColors() {
        return slot.getColors();
    }

    public PlayerSlot getSlot() {
        return slot;
    }
    public void setSlot(PlayerSlot slot) {
        this.slot = slot;
    }

    public PlayerClock getClock() {
        return clock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof Player) {
            if (((Player) o).index == index && index != -1)
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, nick);
    }

    public int getPointsInCategory(GameState state, PointCategory cat) {
    		Option<Integer> value = state.getScore(this).getStats().get(cat);
        return value.isEmpty() ? 0 : value.get();
    }

    public boolean isLocalHuman() {
        return getSlot().isOwn() && !getSlot().isAi();
    }

}
