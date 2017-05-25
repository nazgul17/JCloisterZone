package com.jcloisterzone.immutable;

import java.util.stream.Stream;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;

@Immutable
public class GameState {

	private final Array<Player> players;

    private final HashMap<Player, PlayerScore> score;
    private final Player turnPlayer;

    /*
     *
     * score: Map<Player, Integer> //TODO category tiles: Map<Point, id:String>
     * features: Map<FeaturePointer, Feature> meeples: List<Meeple>
     */

    public GameState(Player[] players) {
    	this.players = Array.of(players);
        this.score = HashMap.ofAll(Stream.of(players), (p) -> new Tuple2<Player, PlayerScore>(p, new PlayerScore()));
        this.turnPlayer = players[0];
    }

    private GameState(Array<Player> players, HashMap<Player, PlayerScore> score, Player turnPlayer) {
    		this.players = players;
        this.score = score;
        this.turnPlayer = turnPlayer;
    }

    private GameState setScore(HashMap<Player, PlayerScore> score) {
        return new GameState(this.players, score, this.turnPlayer);
    }

    public GameState addPoints(Player p, int points, PointCategory category) {
        PlayerScore playerScore = this.score.get(p).get();
        return this.setScore(
            this.score.put(p, playerScore.addPoints(points, category))
        );
    }

    public GameState setTurnPlayer(Player p) {
    		return new GameState(this.players, this.score, turnPlayer);
    }

	public Array<Player> getPlayers() {
		return players;
	}

	public PlayerScore getScore(Player player) {
		return score.get(player).get();
	}

	public Player getTurnPlayer() {
		return turnPlayer;
	}

    // tiles
    // GameState placeTile(Position pos, TileDef tile);
    // GameState



}
