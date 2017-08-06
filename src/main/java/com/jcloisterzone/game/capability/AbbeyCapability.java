package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;

public class AbbeyCapability extends Capability<Void> {

    //private final Set<Player> unusedAbbey = new HashSet<>();
    //private Player abbeyRoundLastPlayer; //when last tile is drawn all players can still place abbey

//    @Override
//    public void initPlayer(Player player) {
//        unusedAbbey.add(player);
//    }

    @Override
    public GameState onStartGame(GameState state) {
        state = state.updatePlayers(ps -> {
            for (Player p : ps.getPlayers()) {
                ps = ps.addPlayerTokenCount(p.getIndex(), Token.ABBEY_TILE, 1);
            }
            return ps;
        });
        return super.onStartGame(state);
    }

    @Override
    public String getTileGroup(TileDefinition tile) {
        return tile.getId().equals(TileDefinition.ABBEY_TILE_ID) ? "inactive": null;
    }

//    public boolean hasUnusedAbbey(Player player) {
//        return unusedAbbey.contains(player);
//    }
//
//    public void useAbbey(Player player) {
//        if (!unusedAbbey.remove(player)) {
//            throw new IllegalArgumentException("Player alredy used his abbey");
//        }
//    }

//    public void undoUseAbbey(Player player) {
//        unusedAbbey.add(player);
//    }

//    public Player getAbbeyRoundLastPlayer() {
//        return abbeyRoundLastPlayer;
//    }
//
//    public void setAbbeyRoundLastPlayer(Player abbeyRoundLastPlayer) {
//        this.abbeyRoundLastPlayer = abbeyRoundLastPlayer;
//    }
}
