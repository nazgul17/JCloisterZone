package com.jcloisterzone.game.capability;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.CompletableFeature;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.TileFeature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

import static com.jcloisterzone.XMLUtils.contentAsLocations;

/**
 * @model Map<Wagon, FeaturePointer> : scored wagons (and unprocessed)
 */
public class WagonCapability extends Capability<Map<Wagon, FeaturePointer>> {

    //private final Map<Player, Feature> scoredWagons = new HashMap<>();

    @Override
    public GameState onStartGame(GameState state) {
        return setModel(state, HashMap.empty());
    }

    @Override
    public List<Follower> createPlayerFollowers(Player player, MeepleIdProvider idProvider) {
        return List.of((Follower) new Wagon(idProvider.generateId(Wagon.class), player));
    }

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("wagon-move");
        assert nl.getLength() <= 1;
        if (nl.getLength() == 1) {
            String tileId = tile.getId();
            Map<Location, Feature> features = tile.getInitialFeatures();
            nl = ((Element) nl.item(0)).getElementsByTagName("neighbouring");
            for (int i = 0; i < nl.getLength(); i++) {
                Array<FeaturePointer> fps = contentAsLocations((Element) nl.item(i))
                    .map(l -> new FeaturePointer(Position.ZERO, l))
                    .toArray();

                for (FeaturePointer fp : fps) {
                    Location loc = fp.getLocation();
                    Completable feature = (Completable) features
                        .find(t -> loc.isPartOf(t._1))
                        .map(Tuple2::_2)
                        .getOrElseThrow(() -> new IllegalStateException(
                            String.format("%s / <wagon-move>: No feature for %s", tileId, loc)
                        ));
                    feature = feature.setNeighboring(fps.remove(fp).toSet());
                    features = features.put(loc, feature);
                }
            }
            tile = tile.setInitialFeatures(features);
        }
        return tile;
    }


    @Override
    public GameState turnPartCleanUp(GameState state) {
        return setModel(state, HashMap.empty());
    }

    public Player getWagonPlayer() {
        if (scoredWagons.isEmpty()) return null;
        int pi = game.getTurnPlayer().getIndex();
        while (!scoredWagons.containsKey(game.getAllPlayers().getPlayer(pi))) {
            pi++;
            if (pi == game.getAllPlayers().length()) pi = 0;
        }
        return game.getAllPlayers().getPlayer(pi);
    }

    private Set<FeaturePointer> filterWagonLocations(Set<FeaturePointer> followerOptions) {
        return Sets.filter(followerOptions, new Predicate<FeaturePointer>() {
            @Override
            public boolean apply(FeaturePointer bp) {
                Feature fe = getBoard().getPlayer(bp);
                return fe instanceof Road || fe instanceof City || fe instanceof Cloister;
            }
        });
    }

    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        if (game.getActivePlayer().hasFollower(Wagon.class) && !followerOptions.isEmpty()) {
            Set<FeaturePointer> wagonLocations = filterWagonLocations(followerOptions);
            if (!wagonLocations.isEmpty()) {
                actions.add(new MeepleAction(Wagon.class).addAll(wagonLocations));
            }
        }
    }
}
