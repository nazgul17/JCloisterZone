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

//    public void replaceState(Reducer... fs) {
//        GameState state = this.state;
//        for (Reducer f : fs) {
//            state = f.apply(state);
//        }
//        replaceState(state);
//    }

    public void replaceState(GameState state) {
        GameState prev = this.state;
        this.state = state;
        post(new GameChangedEvent(prev, state));
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

//    public Undoable getLastUndoable() {
//        return lastUndoable.size() == 0 ? null : lastUndoable.get(lastUndoable.size()-1);
//    }
//

//
//    private boolean isUiSupportedUndo(Event event) {
//        if (event instanceof TileEvent && event.getType() == TileEvent.PLACEMENT) return true;
//        if (event instanceof MeepleEvent && ((MeepleEvent) event).getTo() != null) return true;
//        if (event instanceof BridgeEvent && event.getType() == BridgeEvent.DEPLOY) return true;
//        if (event instanceof GoldChangeEvent) return true;
//        if (event instanceof ScoreEvent && ((ScoreEvent)event).getCategory() == PointCategory.WIND_ROSE) return true;
//        return false;
//    }

    @Override
    public void post(Event event) {
        eventBus.post(event);
        //IMMUTABLE TOTO make state always be not null
 //       eventQueue.add(event);
//        if (event instanceof PlayEvent && !event.isUndo()) {
//            if (isUiSupportedUndo(event)) {
//                if ((event instanceof BridgeEvent && ((BridgeEvent)event).isForced()) ||
//                     event instanceof GoldChangeEvent && ((GoldChangeEvent)event).getPos().equals(getCurrentTile().getPosition()) ||
//                     event instanceof ScoreEvent) {
//                    //just add to chain after tile event
//                    lastUndoable.add((Undoable) event);
//                } else {
//                    lastUndoable.clear();
//                    lastUndoable.add((Undoable) event);
//                    lastUndoablePhase = phase;
//                }
//            } else {
//                if (event.getClass().getAnnotation(Idempotent.class) == null) {
//                    lastUndoable.clear();
//                    lastUndoablePhase = null;
//                }
//            }
//        }
        // process capabilities after undo processing
        // capability can trigger another event and order is important! (eg. windrose scoring)
//        if (event instanceof PlayEvent) {
//            for (Capability capability: getCapabilities()) {
//                capability.handleEvent((PlayEvent) event);
//            }
//        }
    }

//    public void flushEventQueue() {
//        Event event;
//        while ((event = eventQueue.poll()) != null) {
//            eventBus.post(event);
//        }
//    }



//    public void undo() {
//        if (!isUndoAllowed()) {
//            logger.warn("Undo is not allowed");
//            return;
//        }
//        for (int i = lastUndoable.size()-1; i >= 0; i--) {
//            Undoable ev = lastUndoable.get(i);
//            Event inverse = ev.getInverseEvent();
//            inverse.setUndo(true);
//
//            ev.undo(this);
//            post(inverse); //should be post inside undo? silent vs. firing undo?
//        }
//        phase = lastUndoablePhase;
//        lastUndoable.clear();
//        lastUndoablePhase = null;
//        phase.reenter();
//    }


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

    public LinkedHashMap<NeutralFigure<?>, BoardPointer> getDeployedNeutralFigures() {
        return state.getDeployedNeutralFigures();
    }

    @Deprecated
    public Player getTurnPlayer() {
        return state.getTurnPlayer();
    }

    /**
     * Returns player who is allowed to make next action.
     * @return
     */
    @Deprecated
    public Player getActivePlayer() {
        return state.getActivePlayer();
    }

//    public List<NeutralFigure> getNeutralFigures() {
//        return neutralFigures;
//    }

    @Deprecated
    public Player getNextPlayer() {
        return getNextPlayer(getTurnPlayer());
    }

    @Deprecated
    public Player getNextPlayer(Player p) {
        int playerIndex = p.getIndex();
        int nextPlayerIndex = playerIndex == (state.getPlayers().length() - 1) ? 0 : playerIndex + 1;
        return getPlayer(nextPlayerIndex);
    }

    @Deprecated
    public Player getPrevPlayer(Player p) {
        int playerIndex = p.getIndex();
        int prevPlayerIndex = playerIndex == 0 ? state.getPlayers().length() - 1 : playerIndex - 1;
        return getPlayer(prevPlayerIndex);
    }


    /**
     * Return player with the given index.
     * @param index player index
     * @return demand player
     */
    public Player getPlayer(int index) {
        return state.getPlayers().get(index);
    }

    /**
     * Returns whole player list
     * @return player list
     */
    @Deprecated
    public Array<Player> getAllPlayers() {
        return state.getPlayers();
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
    public Seq<Capability> getCapabilities() {
        return state.getCapabilities().values();
    }

    @SuppressWarnings("unchecked")
    public <T extends Capability> T getCapability(Class<T> clazz) {
        return (T) state.getCapabilities().get(clazz).getOrNull();
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

    public TileDefinition initTile(TileDefinition tile, Element xml) {
        for (Capability cap: getCapabilities()) {
            tile = cap.initTile(tile, xml);
        }
        return tile;
    }

    public Feature initFeature(String tileId, Feature feature, Element xml) {
        if (feature instanceof Farm && tileId.startsWith("CO.")) {
            //this is not part of Count capability because it is integral behaviour valid also when capability is off
            feature = ((Farm) feature).setAdjoiningCityOfCarcassonne(true);
        }
        for (Capability cap: getCapabilities()) {
            feature = cap.initFeature(this, tileId, feature, xml);
        }
        return feature;
    }

    public List<Feature> extendFeatures(String tileId) {
        List<Feature> result = List.empty();
        for (Capability cap: getCapabilities()) {
            result.appendAll(cap.extendFeatures(tileId));
        }
        return result;
    }

    public String getTileGroup(TileDefinition tile) {
        for (Capability cap: getCapabilities()) {
            String group = cap.getTileGroup(tile);
            if (group != null) return group;
        }
        return null;
    }

//    public Vector<PlayerAction<?>> prepareActions(Vector<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
//        for (Capability cap: getCapabilities()) {
//            actions = cap.prepareActions(actions, followerOptions);
//        }
//        for (Capability cap: getCapabilities()) {
//            actions = cap.postPrepareActions(actions);
//        }
//        //to simplify capability iterations, allow returning empty actions (eg tower can add empty meeple action when no open tower exists etc)
//        //and then filter them out at end
//        return actions.filter(action -> !action.isEmpty());
//    }


    public boolean isDeployAllowed(Tile tile, Class<? extends Meeple> meepleType) {
        for (Capability cap: getCapabilities()) {
            if (!cap.isDeployAllowed(tile, meepleType)) return false;
        }
        return true;
    }

    public boolean isTilePlacementAllowed(TileDefinition tile, Position p) {
        for (Capability cap: getCapabilities()) {
            if (!cap.isTilePlacementAllowed(tile, p)) return false;
        }
        return true;
    }

    public void saveTileToSnapshot(Tile tile, Document doc, Element tileNode) {
        for (Capability cap: getCapabilities()) {
            cap.saveTileToSnapshot(tile, doc, tileNode);
        }
    }

    public void loadTileFromSnapshot(Tile tile, Element tileNode) {
        for (Capability cap: getCapabilities()) {
            cap.loadTileFromSnapshot(tile, tileNode);
        }
    }

    public CreateGamePhase getCreateGamePhase() {
        return createGamePhase;
    }

    public void setCreateGamePhase(CreateGamePhase createGamePhase) {
        this.createGamePhase = createGamePhase;
    }


}
