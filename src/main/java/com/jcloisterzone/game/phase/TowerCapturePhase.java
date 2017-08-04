package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.CaptureFollowerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.MeeplePrisonEvent;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PassMessage;


@Deprecated //hanlde inside action phase!
public class TowerCapturePhase extends Phase {

    public TowerCapturePhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive(CapabilitiesState capabilities) {
        return capabilities.contains(TowerCapability.class);
    }

    @Override
    public void enter(GameState state) {
        TokenPlacedEvent event = (TokenPlacedEvent) state.getEvents().last();
        assert event.getToken() == Token.TOWER_PIECE;

        Tower = state.getevent.getPointer()

        Position pos = towerCap.getLastIncreasedTower();
        CaptureFollowerAction captureAction = prepareCapture(pos, getBoard().getPlayer(pos).getTower().getHeight());
        if (captureAction.isEmpty()) {
            next();
            return;
        }
        game.post(new SelectActionEvent(getActivePlayer(), captureAction, true));
    }

    private CaptureFollowerAction prepareCapture(Position p, int range) {
        //TODO custom rule - opponent only
        CaptureFollowerAction captureAction = new CaptureFollowerAction();
        for (Meeple pf : game.getDeployedMeeples()) {
            if (!(pf instanceof Follower)) continue;
            Position pos = pf.getPosition();
            if (pos.x != p.x && pos.y != p.y) continue; //check if is in same row or column
            if (pos.squareDistance(p) > range) continue;
            captureAction.add(new MeeplePointer(pf));
        }
        return captureAction;
    }

    @Override
    public void takePrisoner(MeeplePointer mp) {
        Follower m = (Follower) game.getMeeple(mp);
        m.undeploy();
        //undeploy returns figure to owner -> we must handle capture / prisoner exchange
        Player me = getActivePlayer();
        if (m.getPlayer() != me) {
            TowerCapability towerCap = game.get(TowerCapability.class);
            List<Follower> prisoners = towerCap.getPrisoners().get(m.getPlayer());
            List<Follower> myCapturedFollowers = new ArrayList<>();
            for (Follower f : prisoners) {
                if (f.getPlayer() == me) {
                    myCapturedFollowers.add(f);
                }
            }

            if (myCapturedFollowers.isEmpty()) {
                towerCap.inprison(m, me);
            } else {
                //opponent has my prisoner - figure exchange
                Follower exchanged = myCapturedFollowers.get(0); //TODO same type?
                boolean removeOk = prisoners.remove(exchanged);
                assert removeOk;
                exchanged.setInPrison(false);
                game.post(new MeeplePrisonEvent(exchanged, m.getPlayer(), null));
            }
        }
        next();
    }

    @WsSubscribe
    public void handlePass(PassMessage msg) {
        next();
    }
}
