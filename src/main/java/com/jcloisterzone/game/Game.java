package com.jcloisterzone.game;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.HashCode;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.EventProxy;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.GameStartedEvent;
import com.jcloisterzone.event.GameOverEvent;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.setup.SupportedExpansionsChangeEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.phase.AbbeyPhase;
import com.jcloisterzone.game.phase.ActionPhase;
import com.jcloisterzone.game.phase.BazaarPhase;
import com.jcloisterzone.game.phase.CastlePhase;
import com.jcloisterzone.game.phase.CleanUpTurnPartPhase;
import com.jcloisterzone.game.phase.CleanUpTurnPhase;
import com.jcloisterzone.game.phase.CocCountPhase;
import com.jcloisterzone.game.phase.CocFollowerPhase;
import com.jcloisterzone.game.phase.CocPreScorePhase;
import com.jcloisterzone.game.phase.CommitActionPhase;
import com.jcloisterzone.game.phase.CornCirclePhase;
import com.jcloisterzone.game.phase.DragonMovePhase;
import com.jcloisterzone.game.phase.DragonPhase;
import com.jcloisterzone.game.phase.EscapePhase;
import com.jcloisterzone.game.phase.FairyPhase;
import com.jcloisterzone.game.phase.FlierActionPhase;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.game.phase.GoldPiecePhase;
import com.jcloisterzone.game.phase.MageAndWitchPhase;
import com.jcloisterzone.game.phase.PhantomPhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.phase.RequiredCapability;
import com.jcloisterzone.game.phase.ScorePhase;
import com.jcloisterzone.game.phase.TilePhase;
import com.jcloisterzone.game.phase.TowerCapturePhase;
import com.jcloisterzone.game.phase.WagonPhase;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.SlotMessage;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Queue;


/**
 * Other information than board needs in game. Contains players with their
 * points, followers ... and game rules of current game.
 */
//TODO remove extends from GameSettings
public class Game implements EventProxy {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final String gameId;
    private String name;

    private GameSetup setup;
    private GameState state;
    private final ClassToInstanceMap<Phase> phases = MutableClassToInstanceMap.create();

    protected PlayerSlot[] slots;
    protected Expansion[][] slotSupportedExpansions = new Expansion[PlayerSlot.COUNT][];

    private List<GameState> undoState = List.empty();

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
        this.gameId = gameId;
        this.randomSeed = randomSeed;
        this.random = new Random(randomSeed);
    }

    public String getGameId() {
        return gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameState getState() {
        return state;
    }

    public void setSetup(GameSetup setup) {
        this.setup = setup;
    }

    public GameSetup getSetup() {
        return setup;
    }

    public void mapSetup(Function<GameSetup, GameSetup> mapper) {
        setSetup(mapper.apply(setup));
    }

    public void replaceState(GameState state) {
        GameState prev = this.state;
        this.state = state;
        GameChangedEvent ev = new GameChangedEvent(prev, state);
        post(ev);

        if (logger.isInfoEnabled()) {
            StringBuilder sb;
            Queue<PlayEvent> playEvents = ev.getNewPlayEvents();
            if (!playEvents.isEmpty()) {
                sb = new StringBuilder();
                sb.append("play events:");
                for (PlayEvent pev : ev.getNewPlayEvents()) {
                    sb.append("\n  - ");
                    sb.append(pev.toString());
                }
                logger.info(sb.toString());
            }

            ActionsState as = state.getPlayerActions();
            if (as != null) {
                sb = new StringBuilder();
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

    @WsSubscribe
    public void handleSlotMessage(SlotMessage msg) {
        slotSupportedExpansions[msg.getNumber()] = msg.getSupportedExpansions();
        post(new SupportedExpansionsChangeEvent(mergeSupportedExpansions()));
    }

    private EnumSet<Expansion> mergeSupportedExpansions() {
        EnumSet<Expansion> merged = null;
        for (int i = 0; i < slotSupportedExpansions.length; i++) {
            Expansion[] supported = slotSupportedExpansions[i];
            if (supported == null) continue;
            if (merged == null) {
                merged = EnumSet.allOf(Expansion.class);
            }
            EnumSet<Expansion> supp = EnumSet.noneOf(Expansion.class);
            Collections.addAll(supp, supported);
            merged.retainAll(supp);
        }
        return merged;
    }

    @Override
    public void post(Event event) {
        eventBus.post(event);
    }

    //TODO decouple from GameController ?
    public void start(GameController gc) {
        Phase firstPhase = createPhases(gc);
        GameStateBuilder builder = new GameStateBuilder(setup, slots, gc.getConfig());
        // 1. create state with basic config
        replaceState(builder.createInitialState());
        // 2. notify started game - it requires initial state with game config
        post(new GameStartedEvent());
        // 3. trigger initial board changes - make it after started event to propagate all event correctly to GameView
        replaceState(builder.createFirstRoundState(firstPhase));
        firstPhase.enter(this.state);
    }

    private Phase addPhase(GameController gc, Phase next, Class<? extends Phase> phaseClass) {
        RequiredCapability req = phaseClass.getAnnotation(RequiredCapability.class);

        if (req != null && !setup.getCapabilities().contains(req.value())) {
            return next;
        }

        Phase phase;
        try {
            phase = phaseClass.getConstructor(GameController.class).newInstance(gc);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        phases.put(phaseClass, phase);
        if (next != null) {
            phase.setDefaultNext(next);
        }
        return phase;
    }

    protected Phase createPhases(GameController gc) {
        Phase last, next = null;
        //if there isn't assignment - phase is out of standard flow
               addPhase(gc, next, GameOverPhase.class);
        next = last = addPhase(gc, next, CleanUpTurnPhase.class);
        next = addPhase(gc, next, BazaarPhase.class);
        next = addPhase(gc, next, EscapePhase.class);
        next = addPhase(gc, next, CleanUpTurnPartPhase.class);
        next = addPhase(gc, next, CornCirclePhase.class);

        if (setup.getBooleanValue(CustomRule.DRAGON_MOVE_AFTER_SCORING)) {
            addPhase(gc, next, DragonMovePhase.class);
            next = addPhase(gc, next, DragonPhase.class);
        }

               addPhase(gc, next, CocCountPhase.class);
        next = addPhase(gc, next, CocFollowerPhase.class);
        next = addPhase(gc, next, WagonPhase.class);
        next = addPhase(gc, next, ScorePhase.class);
        next = addPhase(gc, next, CocPreScorePhase.class);
        next = addPhase(gc, next, CommitActionPhase.class);
        next = addPhase(gc, next, CastlePhase.class);

        if (!setup.getBooleanValue(CustomRule.DRAGON_MOVE_AFTER_SCORING)) {
               addPhase(gc, next, DragonMovePhase.class);
               next = addPhase(gc, next, DragonPhase.class);
        }

        next = addPhase(gc, next, PhantomPhase.class);
               addPhase(gc, next, TowerCapturePhase.class);
               addPhase(gc, next, FlierActionPhase.class);
        next = addPhase(gc, next, ActionPhase.class);
        next = addPhase(gc, next, MageAndWitchPhase.class);
        next = addPhase(gc, next, GoldPiecePhase.class);
        next = addPhase(gc, next, TilePhase.class);
        next = addPhase(gc, next, AbbeyPhase.class);
        next = addPhase(gc, next, FairyPhase.class);
        last.setDefaultNext(next); //after last phase, the first is default

        return next;
    }

    public void setSlots(PlayerSlot[] slots) {
        this.slots = slots;
    }

    public PlayerSlot[] getPlayerSlots() {
        return slots;
    }

    public Phase getPhase() {
        if (state == null) {
            return null;
        }
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

    public boolean isStarted() {
        return state != null;
    }

    public boolean isOver() {
        return getPhase() instanceof GameOverPhase;
    }

    public int idSequnceNextVal() {
        return ++idSequenceCurrVal;
    }
}
