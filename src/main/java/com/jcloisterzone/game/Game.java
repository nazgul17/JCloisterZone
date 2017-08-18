package com.jcloisterzone.game;

import java.util.Random;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.HashCode;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.EventProxy;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.PlayerTurnEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.phase.CreateGamePhase;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.Reducer;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;


/**
 * Other information than board needs in game. Contains players with their
 * points, followers ... and game rules of current game.
 */
//TODO remove extends from GameSettings
public class Game extends GameSettings implements EventProxy {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    // -- new --

    private GameState state;


    // -- temporary dev --

    private CreateGamePhase createGamePhase;

    // -- old --

//    private final List<NeutralFigure> neutralFigures = new ArrayList<>();
//
    private final ClassToInstanceMap<Phase> phases = MutableClassToInstanceMap.create();

    private List<GameState> undoState = List.empty();

//    private ArrayList<Undoable> lastUndoable = new ArrayList<>();
//    private Phase lastUndoablePhase;

    private final EventBus eventBus = new EventBus(new EventBusExceptionHandler("game event bus"));
    //events are delayed and fired after phase is handled (and eventually switched to the new one) - important especially for AI handlers to not start before switch is done
    //private final java.util.Deque<Event> eventQueue = new java.util.ArrayDeque<>();

    private int idSequenceCurrVal = 0;

    private final Random random;
    private long randomSeed;

    public Game(String gameId) {
        this(gameId, HashCode.fromBytes(gameId.getBytes()).asLong());
    }

    public Game(String gameId, long randomSeed) {
        super(gameId);
        this.randomSeed = randomSeed;
        this.random = new Random(randomSeed);
    }

    public GameState getState() {
        return state;
    }


    public void replaceState(Function<GameState, GameState> f1) {
        replaceState(f1.apply(this.state));
    }

    public void replaceState(Function<GameState, GameState> f1, Function<GameState, GameState> f2) {
        replaceState(f2.apply(f1.apply(this.state)));
    }

    public void replaceState(Function<GameState, GameState> f1, Function<GameState, GameState> f2,
        Function<GameState, GameState> f3) {
        replaceState(f3.apply(f2.apply(f1.apply(this.state))));
    }

    public void replaceState(Function<GameState, GameState> f1, Function<GameState, GameState> f2,
        Function<GameState, GameState> f3, Function<GameState, GameState> f4) {
        replaceState(f4.apply(f3.apply(f2.apply(f1.apply(this.state)))));
    }

    public void replaceState(GameState state) {
        GameState prev = this.state;
        this.state = state;
        post(new GameChangedEvent(prev, state));

        if (logger.isInfoEnabled()) {
            ActionsState as = state.getPlayerActions();
            if (as != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(as.getPlayer().getNick());
                sb.append("'s actions:");
                for (PlayerAction<?> action : as.getActions()) {
                    sb.append("\n  - ");
                    sb.append(action.toString());
                    if (action.getOptions() != null) { // bazaar actions can be empty, handled in differente way
                        sb.append("\n    ");
                        sb.append(String.join(", " , action.getOptions().map(Object::toString)));
                    }
                }
                logger.info(sb.toString());
            }
        }
    }

    @Deprecated
    //call ir directly on state
    public Board getBoard() {
        return state.getBoard();
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    public void markUndo() {
        undoState = undoState.prepend(state);
    }

    public void clearUndo() {
        undoState = List.empty();
    }

    public boolean isUndoAllowed() {
        return !undoState.isEmpty();
    }

    public void undo() {
        if (undoState.isEmpty()) {
            throw new IllegalStateException();
        }
        Tuple2<GameState, List<GameState>> head = undoState.pop2();
        undoState = head._2;
        replaceState(head._1);
    }

    @Override
    public void post(Event event) {
        eventBus.post(event);
    }

    public PlayerSlot[] getPlayerSlots() {
        // need to match subtypes, can't use getInstance on phases
        for (Phase phase : phases.values()) {
            if (phase instanceof CreateGamePhase) {
                return ((CreateGamePhase)phase).getPlayerSlots();
            }
        }
        return null;
    }

    public Phase getPhase() {
        return phases.get(state.getPhase());
    }

    public ClassToInstanceMap<Phase> getPhases() {
        return phases;
    }

    public LinkedHashMap<Meeple, FeaturePointer> getDeployedMeeples() {
        return state.getDeployedMeeples();
    }

    public Random getRandom() {
        return random;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void updateRandomSeed(long update) {
        randomSeed = randomSeed ^ update;
        random.setSeed(randomSeed);
    }

    public Meeple getMeeple(MeeplePointer mp) {
        Tuple2<Meeple, FeaturePointer> match =
            getDeployedMeeples().find(t -> mp.match(t._1)).getOrNull();
        return match == null ? null : match._1;
    }

    @Deprecated
    public boolean isStarted() {
        return !(getPhase() instanceof CreateGamePhase);
    }

    @Deprecated
    public boolean isOver() {
        return getPhase() instanceof GameOverPhase;
    }

    public int idSequnceNextVal() {
        return ++idSequenceCurrVal;
    }

    // delegation to capabilities


//    public boolean isTilePlacementAllowed(TileDefinition tile, Position p) {
//        for (Capability cap: getCapabilities()) {
//            if (!cap.isTilePlacementAllowed(tile, p)) return false;
//        }
//        return true;
//    }

//    public void saveTileToSnapshot(Tile tile, Document doc, Element tileNode) {
//        for (Capability cap: getCapabilities()) {
//            cap.saveTileToSnapshot(tile, doc, tileNode);
//        }
//    }
//
//    public void loadTileFromSnapshot(Tile tile, Element tileNode) {
//        for (Capability cap: getCapabilities()) {
//            cap.loadTileFromSnapshot(tile, tileNode);
//        }
//    }

    public CreateGamePhase getCreateGamePhase() {
        return createGamePhase;
    }

    public void setCreateGamePhase(CreateGamePhase createGamePhase) {
        this.createGamePhase = createGamePhase;
    }


}
