package com.jcloisterzone.game;

import java.io.Serializable;
import java.util.function.Function;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePackState;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.game.phase.Phase;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;
import io.vavr.collection.Seq;

@Immutable
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HashMap<Class<? extends Capability>, Capability> capabilities;

    private final Array<Player> players;
    private final Array<PlayerScore> score;
    private final int turnPlayerIndex;

    private final Array<Seq<Follower>> followers;
    private final Array<Seq<Special>> specialMeeples;
    private final Array<PlayerClock> clocks;

    private final TilePackState tilePack;
    private final TileDefinition drawnTile;

    private final LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles;
    private final List<TileDefinition> discardedTiles;
    private final Map<FeaturePointer, Feature> features;

    private final LinkedHashMap<Meeple, FeaturePointer> deployedMeeples;
    private final LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures;

    private final ActionsState playerActions;
    private final Queue<PlayEvent> events;

    private final Class<? extends Phase> phase;

    public static GameState createInitial(
            HashMap<Class<? extends Capability>, Capability> capabilities,
            Array<Player> players,
            int turnPlayerIndex) {
        return new GameState(
            capabilities,
            players,
            players.map(p -> new PlayerScore()),
            turnPlayerIndex,
            null,
            null,
            null,
            null,
            null,
            LinkedHashMap.empty(),
            List.empty(),
            HashMap.empty(),
            LinkedHashMap.empty(),
            LinkedHashMap.empty(),
            null,
            Queue.empty(),
            null
        );
    }

    private GameState(
            HashMap<Class<? extends Capability>, Capability> capabilities,
            Array<Player> players, Array<PlayerScore> score, int turnPlayerIndex,
            Array<Seq<Follower>> followers,
            Array<Seq<Special>> specialMeeples,
            Array<PlayerClock> clocks,
            TilePackState tilePack, TileDefinition drawnTile,
            LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles,
            List<TileDefinition> discardedTiles, Map<FeaturePointer, Feature> features,
            LinkedHashMap<Meeple, FeaturePointer> deployedMeeples,
            LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures,
            ActionsState playerActions,
            Queue<PlayEvent> events,
            Class<? extends Phase> phase) {
        this.capabilities = capabilities;
        this.players = players;
        this.score = score;
        this.turnPlayerIndex = turnPlayerIndex;
        this.followers = followers;
        this.specialMeeples = specialMeeples;
        this.clocks = clocks;
        this.tilePack = tilePack;
        this.drawnTile = drawnTile;
        this.placedTiles = placedTiles;
        this.discardedTiles = discardedTiles;
        this.features = features;
        this.deployedMeeples = deployedMeeples;
        this.deployedNeutralFigures = deployedNeutralFigures;
        this.playerActions = playerActions;
        this.events = events;
        this.phase = phase;
    }

    public GameState setCapabilities(HashMap<Class<? extends Capability>, Capability> capabilities) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public <C extends Capability> GameState updateCapability(Class<C> cls, Function<C, C> fn) {
        C prev = getCapability(cls);
        C next = fn.apply(prev);
        if (prev == next) {
            return this;
        } else {
            return setCapabilities(capabilities.put(cls, next));
        }
    }

    public GameState setScore(Array<PlayerScore> score) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setTurnPlayerIndex(int turnPlayerIndex) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setFollowers(Array<Seq<Follower>> followers) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setSpecialMeeples(Array<Seq<Special>> specialMeeples) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setClocks(Array<PlayerClock> clocks) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setTilePack(TilePackState tilePack) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setDrawnTile(TileDefinition drawnTile) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setPlacedTiles(LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setFeatures(Map<FeaturePointer, Feature> features) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setDiscardedTiles(List<TileDefinition> discardedTiles) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setDeployedMeeples(LinkedHashMap<Meeple, FeaturePointer> deployedMeeples) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setDeployedNeutralFigures(LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setPlayerActions(ActionsState playerActions) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setEvents(Queue<PlayEvent> events) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState setPhase(Class<? extends Phase> phase) {
        return new GameState(
            capabilities,
            players, score, turnPlayerIndex,
            followers, specialMeeples, clocks,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events,
            phase
        );
    }

    public GameState appendEvent(PlayEvent ev) {
        return setEvents(events.append(ev));
    }

    public HashMap<Class<? extends Capability>, Capability> getCapabilities() {
        return capabilities;
    }

    @SuppressWarnings("unchecked")
    public <C extends Capability> C getCapability(Class<C> cls) {
        return (C) capabilities.get(cls).getOrNull();
    }

    public boolean hasCapability(Class<? extends Capability> cls) {
        return capabilities.containsKey(cls);
    }

    public Array<Player> getPlayers() {
        return players;
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

    public LinkedHashMap<NeutralFigure<?>, BoardPointer> getDeployedNeutralFigures() {
        return deployedNeutralFigures;
    }

    public ActionsState getPlayerActions() {
        return playerActions;
    }

    public Queue<PlayEvent> getEvents() {
        return events;
    }

    public Class<? extends Phase> getPhase() {
        return phase;
    }

    // ------ helpers -------------

    private Board board;

    public Player getTurnPlayer() {
        return players.get(turnPlayerIndex);
    }

    public Player getActivePlayer() {
        if (playerActions == null) {
            return null;
        }
        return playerActions.getPlayer();
    }

    public Board getBoard() {
        if (board == null) board = new Board(this);
        return board;
    }

    public boolean isGameOver() {
        return GameOverPhase.class.equals(phase);
    }
}
