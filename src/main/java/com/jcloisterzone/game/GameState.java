package com.jcloisterzone.game;

import com.jcloisterzone.IPlayer;
import com.jcloisterzone.Immutable;
import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;

@Immutable
public class GameState {

    private final Array<PlayerAttributes> players;
    private final Array<PlayerScore> score;
    private final int turnPlayer;

    private final HashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles;
    private final List<TileDefinition> discardedTiles;

    /*
     * tilepack = List<TileDefinition> TOOD random remove, implement own class
     * board : Map<Position, (TileDefinition, Rotation)>
     * features: Map<FeaturePointer, Feature>
     * meeples: Map<id: String, FeaturePointer>
     */

    public static GameState createInitial(Array<PlayerAttributes> players, int turnPlayer) {
        return new GameState(
            players, players.map(p -> new PlayerScore()), turnPlayer,
            HashMap.empty(),
            List.empty()
        );
    }

    private GameState(Array<PlayerAttributes> players, Array<PlayerScore> score, int turnPlayer,
            HashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles,
            List<TileDefinition> discardedTiles) {
        this.players = players;
        this.score = score;
        this.turnPlayer = turnPlayer;
        this.placedTiles = placedTiles;
        this.discardedTiles = discardedTiles;
    }

    private GameState setScore(Array<PlayerScore> score) {
        return new GameState(this.players, score, this.turnPlayer, this.placedTiles, this.discardedTiles);
    }

    public GameState addPoints(IPlayer p, int points, PointCategory category) {
        PlayerScore playerScore = this.score.get(p.getIndex());
        return this.setScore(
            this.score.insert(p.getIndex(), playerScore.addPoints(points, category))
        );
    }

    public GameState setTurnPlayer(int turnPlayer) {
        return new GameState(players, score, turnPlayer, placedTiles, discardedTiles);
    }

    public GameState setPlacedTiles(HashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles) {
        return new GameState(players, score, turnPlayer, placedTiles, discardedTiles);
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

    public HashMap<Position, Tuple2<TileDefinition, Rotation>> getPlacedTiles() {
        return placedTiles;
    }
}
