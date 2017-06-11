package com.jcloisterzone.feature;

import java.util.Map.Entry;
import java.util.function.Predicate;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;

import io.vavr.Predicates;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public abstract class ScoreableFeature extends TileFeature implements Scoreable {

    public ScoreableFeature(Game game, List<FeaturePointer> places) {
        super(game, places);
    }

    protected int getPower(Follower f) {
        return f.getPower();
    }

    @Override
    public abstract int getPoints(Player player);

    public Set<Player> getOwners() {
        HashMap<PlayerAttributes, Integer> powers = getFollowers()
            .foldLeft(HashMap.<PlayerAttributes, Integer>empty(), (acc, m) -> {
                PlayerAttributes player = m.getPlayer();
                int power = getPower(m);
                return acc.put(player, acc.get(player).getOrElse(0) + power);
            });

        Integer maxPower = powers.values().max().getOrNull();
        return powers.keySet()
            .filter(p -> powers.get(p).get() == maxPower)
            .map(game::getPlayer);
    }

    @Override
    public Follower getSampleFollower(Player player) {
        return getFollowers().find(f -> f.getPlayer().equals(player)).getOrNull();
    }

    //helpers

    protected int getMageAndWitchPoints(int points) {
        Stream<Special> specials = getSpecialMeeples();
        if (!specials.find(Predicates.instanceOf(Mage.class)).isEmpty()) {
            points += getPlaces().size();
        }
        if (!specials.find(Predicates.instanceOf(Witch.class)).isEmpty()) {
            if (points % 2 == 1) points++;
            points /= 2;
        }
        return points;
    }

    protected int getLittleBuildingPoints() {
        if (!game.hasCapability(LittleBuildingsCapability.class)) return 0;
        int points = 0;
        // IMMUTABLE TODO
//        for (Entry<LittleBuilding, Integer> entry : littleBuildings.entrySet()) {
//            if (game.getBooleanValue(CustomRule.BULDINGS_DIFFERENT_VALUE)) {
//                LittleBuilding lb = entry.getKey();
//                switch (lb) {
//                    case SHED: points += entry.getValue(); break;
//                    case HOUSE: points += 2*entry.getValue(); break;
//                    case TOWER: points += 3*entry.getValue(); break;
//                }
//            } else {
//                points += entry.getValue();
//            }
//        }
        return points;
    }
}
