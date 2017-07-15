package com.jcloisterzone.game;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePackState;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.PlayEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.NeutralFigure;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;

@Immutable
public class GameState {

    private final HashMap<Class<? extends Capability>, Capability> capabilities;

    private final Array<PlayerAttributes> players;
    private final Array<PlayerScore> score;
    private final int turnPlayer;

    private final TilePackState tilePack;
    private final TileDefinition drawnTile;

    private final LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles;
    private final List<TileDefinition> discardedTiles;
    private final Map<FeaturePointer, Feature> features;

    private final LinkedHashMap<Meeple, FeaturePointer> deployedMeeples;
    private final LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures;

    private final ActionsState playerActions;
    private final Queue<PlayEvent> events;

    public static GameState createInitial(
            HashMap<Class<? extends Capability>, Capability> capabilities,
            Array<PlayerAttributes> players,
            int turnPlayer) {
        return new GameState(
            capabilities,
            players,
            players.map(p -> new PlayerScore()),
            turnPlayer,
            null,
            null,
            LinkedHashMap.empty(),
            List.empty(),
            HashMap.empty(),
            LinkedHashMap.empty(),
            LinkedHashMap.empty(),
            null,
            Queue.empty()
        );
    }

    private GameState(
            HashMap<Class<? extends Capability>, Capability> capabilities,
            Array<PlayerAttributes> players, Array<PlayerScore> score, int turnPlayer,
            TilePackState tilePack, TileDefinition drawnTile,
            LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles,
            List<TileDefinition> discardedTiles, Map<FeaturePointer, Feature> features,
            LinkedHashMap<Meeple, FeaturePointer> deployedMeeples,
            LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures,
            ActionsState playerActions,
            Queue<PlayEvent> events) {
        this.capabilities = capabilities;
        this.players = players;
        this.score = score;
        this.turnPlayer = turnPlayer;
        this.tilePack = tilePack;
        this.drawnTile = drawnTile;
        this.placedTiles = placedTiles;
        this.discardedTiles = discardedTiles;
        this.features = features;
        this.deployedMeeples = deployedMeeples;
        this.deployedNeutralFigures = deployedNeutralFigures;
        this.playerActions = playerActions;
        this.events = events;
    }

    public GameState setCapabilities(HashMap<Class<? extends Capability>, Capability> capabilities) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setScore(Array<PlayerScore> score) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setTurnPlayer(int turnPlayer) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setTilePack(TilePackState tilePack) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setDrawnTile(TileDefinition drawnTile) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setPlacedTiles(LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setFeatures(Map<FeaturePointer, Feature> features) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setDiscardedTiles(List<TileDefinition> discardedTiles) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setDeployedMeeples(LinkedHashMap<Meeple, FeaturePointer> deployedMeeples) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setDeployedNeutralFigures(LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setPlayerAcrions(ActionsState playerActions) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState setEvents(Queue<PlayEvent> events) {
        return new GameState(
            capabilities,
            players, score, turnPlayer, tilePack, drawnTile, placedTiles, discardedTiles,
            features, deployedMeeples, deployedNeutralFigures, playerActions, events
        );
    }

    public GameState appendEvent(PlayEvent ev) {
        return setEvents(events.append(ev));
    }

    public HashMap<Class<? extends Capability>, Capability> getCapabilities() {
        return capabilities;
    }

    public Capability getCapability(Class<? extends Capability> cls) {
        return capabilities.get(cls).getOrNull();
    }

    public Array<PlayerAttributes> getPlayers() {
        return players;
    }

    public Array<PlayerScore> getScore() {
        return score;
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

    public LinkedHashMap<NeutralFigure<?>, BoardPointer> getDeployedNeutralFigures() {
        return deployedNeutralFigures;
    }

    public ActionsState getPlayerActions() {
        return playerActions;
    }

    public Queue<PlayEvent> getEvents() {
        return events;
    }

    // ------ helpers -------------

    private Board board;

    public PlayerAttributes getActivePlayer() {
        if (playerActions != null) {
            return playerActions.getPlayer();
        }
        return players.get(turnPlayer);
    }

    public Board getBoard() {
        if (board == null) board = new Board(this);
        return board;
    }

    // --- TODO ---

    public boolean isGameOver() {
        //TODO base on current phase
        return false;
    }
}
