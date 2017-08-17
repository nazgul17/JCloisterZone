package com.jcloisterzone.game.phase;

import java.util.Arrays;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.BuilderState;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.PlaceTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PlaceTileMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.Stream;

public class AbbeyPhase extends ServerAwarePhase {

    public AbbeyPhase(Game game, GameController controller) {
        super(game, controller);
    }

    @Override
    public boolean isActive(CapabilitiesState capabilities) {
        return capabilities.contains(AbbeyCapability.class);
    }

    @Override
    public void enter(GameState state) {
        BazaarCapabilityModel bazaarModel = state.getCapabilities().getModel(BazaarCapability.class);
        BuilderState builderState = state.getCapabilities().getModel(BuilderCapability.class);
        boolean baazaarInProgress = bazaarModel != null && !bazaarModel.getSupply().isEmpty();
        boolean builderSecondTurnPart = builderState == BuilderState.SECOND_TURN;
        boolean hasAbbey = state.getPlayers().getPlayerTokenCount(state.getPlayers().getTurnPlayerIndex(), Token.ABBEY_TILE) > 0;
        if (hasAbbey && (builderSecondTurnPart || !baazaarInProgress)) {
            Stream<Tuple2<Position, EdgePattern>> holes = state.getBoard().getHoles();
            if (!holes.isEmpty()) {
                toggleClock(state.getTurnPlayer());

                TileDefinition abbey = state.getTilePack().findTile(TileDefinition.ABBEY_TILE_ID).get();

                TilePlacementAction action = new TilePlacementAction(
                    abbey,
                    holes.flatMap(t ->
                        Array.ofAll(Arrays.asList(Rotation.values()))
                            .map(r -> new TilePlacement(t._1, r, null))
                    ).toSet()
                );

                state = state.setPlayerActions(new ActionsState(
                    state.getTurnPlayer(),
                    action,
                    true
                ));

                promote(state);
                return;
            }
        }
        next(state);
    }

    @WsSubscribe
    public void handlePlaceTile(PlaceTileMessage msg) {
        if (!msg.getTileId().equals(TileDefinition.ABBEY_TILE_ID)) {
            throw new IllegalArgumentException("Only abbey can be placed.");
        }

        GameState state = game.getState();
        Player player = state.getActivePlayer();
        state = state.updatePlayers(ps ->
            ps.addPlayerTokenCount(player.getIndex(), Token.ABBEY_TILE, -1)
        );

        TileDefinition abbey = state.getTilePack().findTile(TileDefinition.ABBEY_TILE_ID).get();
        state = (new PlaceTile(abbey, msg.getPosition(), msg.getRotation())).apply(state);
        state = clearActions(state);

        next(state, ActionPhase.class);
    }
}
