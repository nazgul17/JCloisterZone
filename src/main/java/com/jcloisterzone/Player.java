package com.jcloisterzone;

import java.io.Serializable;
import java.lang.reflect.Modifier;

import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.ui.PlayerColor;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;


/**
 * Represents one player in game. Contains information about figures, points and
 * control informations.<br>
 *
 * @author Roman Krejcik
 */
public class Player implements IPlayer, Serializable {

    private static final long serialVersionUID = -7276471952562769832L;

    private final Game game;
    private final PlayerAttributes attributes;

    private final List<Follower> followers;
    private final List<Special> specialMeeples;

//    private final List<Follower> followers = new ArrayList<Follower>(SmallFollower.QUANTITY + 3);
//    private final List<Special> specialMeeples = new ArrayList<Special>(3);
//    private final Iterable<Meeple> meeples = Iterables.<Meeple>concat(followers, specialMeeples);

    private final PlayerClock clock = new PlayerClock();

    public Player(Game game, PlayerAttributes attributes) {
        this.game = game;
        this.attributes = attributes;

        followers = game.createPlayerFollowers(attributes);
        specialMeeples = game.createPlayerSpecialMeeples(attributes);
    }

    public List<Follower> getFollowers() {
        return followers;
    }

    public List<Special> getSpecialMeeples() {
        return specialMeeples;
    }

    public Stream<Meeple> getMeeples() {
        return Stream.concat(followers, specialMeeples);
    }

    public boolean hasSpecialMeeple(Class<? extends Special> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        return !Stream.ofAll(specialMeeples)
            .filter(m -> m.getClass().equals(clazz))
            .filter(m -> m.isInSupply())
            .isEmpty();
    }

    public boolean hasFollower() {
        return !Stream.ofAll(followers)
            .filter(m -> m.isInSupply())
            .isEmpty();
    }

    public boolean hasFollower(Class<? extends Follower> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        //check equality not instanceOf - phantom is subclass of small follower
        return !Stream.ofAll(followers)
                .filter(m -> m.getClass().equals(clazz))
                .filter(m -> m.isInSupply())
                .isEmpty();
    }

    public Meeple getMeepleFromSupply(Class<? extends Meeple> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        List<? extends Meeple> collection = (Follower.class.isAssignableFrom(clazz) ? followers : specialMeeples);
        return Stream.ofAll(collection)
            .filter(m -> m.getClass().equals(clazz))
            .find(m -> m.isInSupply())
            .getOrNull();
    }

    public void addPoints(int points, PointCategory category) {
        if (points != 0) {
            game.replaceState(state -> state.addPoints(this, points, category));
        }
    }

    public int getPoints() {
        return game.getState().getScore(this).getPoints();
    }

    public String getNick() {
        return attributes.getNick();
    }

    @Override
    public String toString() {
        return attributes.getNick() + " " + getPoints();
    }

    public int getIndex() {
        return attributes.getIndex();
    }

    public PlayerColor getColors() {
        return attributes.getColors();
    }

    public PlayerSlot getSlot() {
        return attributes.getSlot();
    }

    public PlayerClock getClock() {
        return clock;
    }

    public int getPointsInCategory(PointCategory cat) {
        HashMap<PointCategory, Integer> pointStats = game.getState().getScore(this).getStats();
        Option<Integer> points = pointStats.get(cat);
        return points.getOrElse(0);
    }

    public boolean isLocalHuman() {
        return attributes.isLocalHuman();
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

    @Override
    public int hashCode() {
        return attributes.hashCode();
    }



}
