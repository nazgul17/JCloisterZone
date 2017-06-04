package com.jcloisterzone.game;

import com.jcloisterzone.IPlayer;
import com.jcloisterzone.Immutable;
import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

@Immutable
public class GameState {

    private final Array<PlayerAttributes> players;
    private final Array<PlayerScore> score;
    private final int turnPlayer;

    private final TilePack tilePack;
    private final Map<Position, Tuple2<TileDefinition, Rotation>> placedTiles;
    private final List<TileDefinition> discardedTiles;
    private final Map<FeaturePointer, Feature> features;

    /*
     * tilepack = List<TileDefinition> TOOD random remove, implement own class
     * board : Map<Position, (TileDefinition, Rotation)>
     * features: Map<FeaturePointer, Feature>
     * meeples: Map<id: String, FeaturePointer>
     */

    public static GameState createInitial(Array<PlayerAttributes> players, int turnPlayer) {
        return new GameState(
            players, players.map(p -> new PlayerScore()), turnPlayer,
            null,
            HashMap.empty(),
            List.empty(),
            HashMap.empty()
        );
    }

    private GameState(Array<PlayerAttributes> players, Array<PlayerScore> score, int turnPlayer,
            TilePack tilePack,
            Map<Position, Tuple2<TileDefinition, Rotation>> placedTiles,
            List<TileDefinition> discardedTiles, Map<FeaturePointer, Feature> features) {
        this.players = players;
        this.score = score;
        this.turnPlayer = turnPlayer;
        this.tilePack = tilePack;
        this.placedTiles = placedTiles;
        this.discardedTiles = discardedTiles;
        this.features = features;
    }

    private GameState setScore(Array<PlayerScore> score) {
        return new GameState(players, score, turnPlayer, tilePack, placedTiles, discardedTiles, features);
    }

    public GameState addPoints(IPlayer p, int points, PointCategory category) {
        PlayerScore playerScore = this.score.get(p.getIndex());
        return this.setScore(
            this.score.insert(p.getIndex(), playerScore.addPoints(points, category))
        );
    }

    public GameState setTurnPlayer(int turnPlayer) {
        return new GameState(players, score, turnPlayer, tilePack, placedTiles, discardedTiles, features);
    }

    public GameState setTilePacks(TilePack tilePack) {
        return new GameState(players, score, turnPlayer, tilePack, placedTiles, discardedTiles, features);
    }

    public GameState setPlacedTiles(Map<Position, Tuple2<TileDefinition, Rotation>> placedTiles) {
        return new GameState(players, score, turnPlayer, tilePack, placedTiles, discardedTiles, features);
    }

    public GameState setFeatures(Map<FeaturePointer, Feature> features) {
        return new GameState(players, score, turnPlayer, tilePack, placedTiles, discardedTiles, features);
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

    public TilePack getTilePack() {
        return tilePack;
    }

    public Map<Position, Tuple2<TileDefinition, Rotation>> getPlacedTiles() {
        return placedTiles;
    }

    public Map<FeaturePointer, Feature> getFeatures() {
        return features;
    }
}
