package com.jcloisterzone.game;

import com.jcloisterzone.IPlayer;
import com.jcloisterzone.Immutable;
import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePackState;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;

@Immutable
public class GameState {

    private final Array<PlayerAttributes> players;
    private final Array<PlayerScore> score;
    private final int turnPlayer;

    private final TilePackState tilePack;
    private final TileDefinition drawnTile;

    private final LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles;
    private final List<TileDefinition> discardedTiles;
    private final Map<FeaturePointer, Feature> features;

    private final LinkedHashMap<Meeple, FeaturePointer> deployedMeeples;

    private final Queue<Event> events;

    public static GameState createInitial(Array<PlayerAttributes> players, int turnPlayer) {
        return new GameState(
            players, players.map(p -> new PlayerScore()), turnPlayer,
            null,
            null,
            LinkedHashMap.empty(),
            List.empty(),
            HashMap.empty(),
            LinkedHashMap.empty(),
            Queue.empty()
        );
    }

    private GameState(Array<PlayerAttributes> players, Array<PlayerScore> score, int turnPlayer,
            TilePackState tilePack, TileDefinition drawnTile,
            LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles,
            List<TileDefinition> discardedTiles, Map<FeaturePointer, Feature> features,
            LinkedHashMap<Meeple, FeaturePointer> deployedMeeples,
            Queue<Event> events) {
        this.players = players;
        this.score = score;
        this.turnPlayer = turnPlayer;
        this.tilePack = tilePack;
        this.drawnTile = drawnTile;
        this.placedTiles = placedTiles;
        this.discardedTiles = discardedTiles;
        this.features = features;
        this.deployedMeeples = deployedMeeples;
        this.events = events;
    }

    private GameState setScore(Array<PlayerScore> score) {
        return new GameState(players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles, features, deployedMeeples, events);
    }

    public GameState addPoints(IPlayer p, int points, PointCategory category) {
        PlayerScore playerScore = this.score.get(p.getIndex());
        return this.setScore(
            this.score.insert(p.getIndex(), playerScore.addPoints(points, category))
        );
    }

    public GameState setTurnPlayer(int turnPlayer) {
        return new GameState(players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles, features, deployedMeeples, events);
    }

    public GameState setTilePack(TilePackState tilePack) {
        return new GameState(players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles, features, deployedMeeples, events);
    }

    public GameState setDrawnTile(TileDefinition drawnTile) {
        return new GameState(players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles, features, deployedMeeples, events);
    }

    public GameState setPlacedTiles(LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles) {
        return new GameState(players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles, features, deployedMeeples, events);
    }

    public GameState setFeatures(Map<FeaturePointer, Feature> features) {
        return new GameState(players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles, features, deployedMeeples, events);
    }

    public GameState setDiscardedTiles(List<TileDefinition> discardedTiles) {
        return new GameState(players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles, features, deployedMeeples, events);
    }

    public GameState setDeployedMeeples(LinkedHashMap<Meeple, FeaturePointer> deployedMeeples) {
        return new GameState(players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles, features, deployedMeeples, events);
    }

    public GameState setEvents(Queue<Event> events) {
        return new GameState(players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles, features, deployedMeeples, events);
    }

    public Array<PlayerAttributes> getPlayers() {
        return players;
    }

    public PlayerScore getScore(IPlayer player) {
        return score.get(player.getIndex());
    }

    public int getTurnPlayer() {
        return turnPlayer;
    }

    public TilePackState getTilePack() {
        return tilePack;
    }

    public TileDefinition getDrawnTile() {
        return drawnTile;
    }

    public LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> getPlacedTiles() {
        return placedTiles;
    }

    public List<TileDefinition> getDiscardedTiles() {
        return discardedTiles;
    }

    public Map<FeaturePointer, Feature> getFeatures() {
        return features;
    }

    public LinkedHashMap<Meeple, FeaturePointer> getDeployedMeeples() {
        return deployedMeeples;
    }

    public Queue<Event> getEvents() {
        return events;
    }
}
