package com.jcloisterzone.game;

import com.jcloisterzone.IPlayer;
import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.PointCategory;

import io.vavr.collection.Array;

@Immutable
public class GameState {

    private final Array<PlayerAttributes> players;
    private final Array<PlayerScore> score;
    private final int turnPlayer;

    /*
     * tilepack = List<TileDefinition> TOOD random remove, implement own class
     * board : Map<Position, TileDefinition>
     * features: Map<FeaturePointer, Feature>
     * meeples: Map<id: String, FeaturePointer>
     */

    public static GameState createInitial(Array<PlayerAttributes> players, int turnPlayer) {
        return new GameState(
            players, players.map(p -> new PlayerScore()), turnPlayer
        );
    }

    private GameState(Array<PlayerAttributes> players, Array<PlayerScore> score, int turnPlayer) {
        this.players = players;
        this.score = score;
        this.turnPlayer = turnPlayer;
    }

    private GameState setScore(Array<PlayerScore> score) {
        return new GameState(this.players, score, this.turnPlayer);
    }

    public GameState addPoints(IPlayer p, int points, PointCategory category) {
        PlayerScore playerScore = this.score.get(p.getIndex());
        return this.setScore(
            this.score.insert(p.getIndex(), playerScore.addPoints(points, category))
        );
    }

    public GameState setTurnPlayer(int turnPlayer) {
        return new GameState(this.players, this.score, turnPlayer);
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

    // tiles
    // GameState placeTile(Position pos, TileDef tile);
    // GameState



}
