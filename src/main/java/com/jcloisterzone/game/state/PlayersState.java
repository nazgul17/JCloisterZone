package com.jcloisterzone.game.state;

import java.io.Serializable;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Special;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;

@Immutable
public class PlayersState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Array<Player> players;
    private final Array<PlayerScore> score;
    private final int turnPlayerIndex;

    private final Array<Seq<Follower>> followers;
    private final Array<Seq<Special>> specialMeeples;
    private final Array<PlayerClock> clocks;

    public static PlayersState createInitial(Array<Player> players, int turnPlayerIndex) {
        return new PlayersState(
            players,
            players.map(p -> new PlayerScore()),
            turnPlayerIndex,
            null,
            null,
            null
        );
    }

    public PlayersState(
            Array<Player> players, Array<PlayerScore> score, int turnPlayerIndex,
            Array<Seq<Follower>> followers,
            Array<Seq<Special>> specialMeeples,
            Array<PlayerClock> clocks) {
        this.players = players;
        this.score = score;
        this.turnPlayerIndex = turnPlayerIndex;
        this.followers = followers;
        this.specialMeeples = specialMeeples;
        this.clocks = clocks;
    }

    public PlayersState setScore(Array<PlayerScore> score) {
        return new PlayersState(
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks
        );
    }

    public PlayersState setTurnPlayerIndex(int turnPlayerIndex) {
        return new PlayersState(
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks
        );
    }

    public PlayersState setFollowers(Array<Seq<Follower>> followers) {
        return new PlayersState(
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks
        );
    }

    public PlayersState setSpecialMeeples(Array<Seq<Special>> specialMeeples) {
        return new PlayersState(
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks
        );
    }

    public PlayersState setClocks(Array<PlayerClock> clocks) {
        return new PlayersState(
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks
        );
    }

    public Array<Player> getPlayers() {
        return players;
    }

    public Player getPlayer(int idx) {
        return players.get(idx);
    }

    public int length() {
        return players.length();
    }

    public Array<PlayerScore> getScore() {
        return score;
    }

    public int getTurnPlayerIndex() {
        return turnPlayerIndex;
    }

    public Array<Seq<Follower>> getFollowers() {
        return followers;
    }

    public Array<Seq<Special>> getSpecialMeeples() {
        return specialMeeples;
    }

    public Array<PlayerClock> getClocks() {
        return clocks;
    }

    public Player getTurnPlayer() {
        return getPlayer(turnPlayerIndex);
    }
}
