package com.jcloisterzone.game.phase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Application;
import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.wsio.RmiProxy;


public abstract class Phase implements RmiProxy {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected final Game game;

    private Phase defaultNext;

    public Phase(Game game) {
        this.game = game;
    }

    public Phase getDefaultNext() {
        return defaultNext;
    }

    public void setDefaultNext(Phase defaultNext) {
        this.defaultNext = defaultNext;
    }

    public void next(GameState state) {
        next(state, defaultNext);
    }

    public void next(GameState state, Class<? extends Phase> phaseClass) {
        next(state, game.getPhases().get(phaseClass));
    }

    public void next(GameState state, Phase phase) {
        phase.enter(state);
    }

    public abstract void enter(GameState state);

    protected void promote(GameState state) {
        state = state.setPhase(this.getClass());
        game.replaceState(state);
    }

    /**
     * Method is invoked on active phase when user buy back inprisoned follower
     */
    @Deprecated //generic approach to refresh actions
    public void notifyRansomPaid() {
        //do nothing by default
    }

    public boolean isActive() {
        return true;
    }

    //shortcuts


    /** handler called after game is load if this phase is active */
    public void loadGame(Snapshot snapshot) {
        //do nothing by default
    }

    //adapter methods

    @Override
    public void pass() {
        logger.error(Application.ILLEGAL_STATE_MSG, "pass");
    }

    @Override
    public void placeTile(Rotation  rotation, Position position) {
        logger.error(Application.ILLEGAL_STATE_MSG, "placeTile");
    }

    @Override
    public void deployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType) {
        logger.error(Application.ILLEGAL_STATE_MSG, "deployMeeple");
    }

    @Override
    public void placeTowerPiece(Position p) {
        logger.error(Application.ILLEGAL_STATE_MSG, "placeTowerPiece");
    }

    @Override
    public void placeTunnelPiece(FeaturePointer fp, boolean isSecondPiece) {
        logger.error(Application.ILLEGAL_STATE_MSG, "placeTunnelPiece");
    }

    @Override
    public void undeployMeeple(MeeplePointer mp) {
        logger.error(Application.ILLEGAL_STATE_MSG, "undeployMeeple");
    }

    @Override
    public void moveNeutralFigure(BoardPointer prt, Class<? extends NeutralFigure> figureType) {
        logger.error(Application.ILLEGAL_STATE_MSG, "moveNeutralFigure");

    }

    @Override
    public final void payRansom(Integer playerIndexToPay, Class<? extends Follower> meepleType) {
        //pay ransom is valid any time
        TowerCapability towerCap = game.getCapability(TowerCapability.class);
        if (towerCap == null) {
            logger.error(Application.ILLEGAL_STATE_MSG, "payRansom");
            return;
        }
        towerCap.payRansom(playerIndexToPay, meepleType);
    }

    @Override
    public void takePrisoner(MeeplePointer mp) {
         logger.error(Application.ILLEGAL_STATE_MSG, "takePrisoner");
    }

    @Override
    public void deployBridge(Position pos, Location loc) {
        logger.error(Application.ILLEGAL_STATE_MSG, "deployBridge");

    }

    @Override
    public void deployCastle(Position pos, Location loc) {
        logger.error(Application.ILLEGAL_STATE_MSG, "deployCastle");
    }

    @Override
    public void bazaarBid(Integer supplyIndex, Integer price) {
        logger.error(Application.ILLEGAL_STATE_MSG, "bazaarBid");
    }

    @Override
    public void bazaarBuyOrSell(boolean buy) {
        logger.error(Application.ILLEGAL_STATE_MSG, "bazaarBuyOrSell");
    }

    @Override
    public void cornCiclesRemoveOrDeploy(boolean remove) {
        logger.error(Application.ILLEGAL_STATE_MSG, "cornCiclesRemoveOrDeploy");
    }

    @Override
    public void placeLittleBuilding(LittleBuilding lbType) {
        logger.error(Application.ILLEGAL_STATE_MSG, "placeLittleBuilding");
    }

    @Override
    public void placeGoldPiece(Position pos) {
        logger.error(Application.ILLEGAL_STATE_MSG, "placeGoldPiece");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
