package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameState;

import io.vavr.collection.Vector;

@Immutable
public class DragonCapability extends Capability {

    private static final long serialVersionUID = 1L;

    public static final int DRAGON_MOVES = 6;

    // Visited positions can be derived
    private final Vector<Location> dragonMoves;
//    private int dragonMovesLeft;
//    private Player dragonPlayer;
//    private Set<Position> dragonVisitedTiles;

    public DragonCapability() {
        this(Vector.empty());
    }

    public DragonCapability(Vector<Location> dragonMoves) {
        this.dragonMoves = dragonMoves;
    }

    public DragonCapability setDragonMoves(Vector<Location> dragonMoves) {
        return new DragonCapability(dragonMoves);
    }

    public Vector<Location> getDragonMoves() {
        return dragonMoves;
    }

    @Override
    public GameState onStartGame(GameState state) {
        return state.appendNeutralFigure(new Dragon());
    }

    @Override
    public GameState onTilePlaced(GameState state) {
        Tile tile = state.getBoard().getLastPlaced();
        if (tile.hasTrigger(TileTrigger.VOLCANO)) {
            state = state.setTilePack(
                state.getTilePack().setGroupState("dragon", TileGroupState.ACTIVE);
            );
            //dragon.deploy(tile.getPosition());
        }
    }

    public Dragon getDragon() {
        return dragon;
    }

    @Override
    public String getTileGroup(Tile tile) {
        return tile.hasTrigger(TileTrigger.DRAGON) ? "dragon" : null;
    }

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("volcano").getLength() > 0) {
            tile.setTrigger(TileTrigger.VOLCANO);
        }
        if (xml.getElementsByTagName("dragon").getLength() > 0) {
            tile.setTrigger(TileTrigger.DRAGON);
        }
    }

    @Override
    public boolean isDeployAllowed(GameState state, Position pos) {
        return !dragon.at(pos);
    }

    public Player getDragonPlayer() {
        return dragonPlayer;
    }

    public int getDragonMovesLeft() {
        return dragonMovesLeft;
    }

    public Set<Position> getDragonVisitedTiles() {
        return dragonVisitedTiles;
    }

    public void triggerDragonMove() {
        dragonMovesLeft = DRAGON_MOVES;
        dragonPlayer = game.getTurnPlayer();
        dragonVisitedTiles = new HashSet<>();
        dragonVisitedTiles.add(dragon.getPosition());
    }

    public void endDragonMove() {
        dragonMovesLeft = 0;
        dragonVisitedTiles = null;
        dragonPlayer = null;
    }

    public void moveDragon(Position p) {
        dragonVisitedTiles.add(p);
        dragonPlayer = game.getNextPlayer(dragonPlayer);
        dragonMovesLeft--;
        dragon.deploy(p);
    }

    public Set<Position> getAvailDragonMoves() {
        Set<Position> result = new HashSet<>();
        FairyCapability fairyCap = game.getCapability(FairyCapability.class);
        for (Position offset: Position.ADJACENT.values()) {
            Position position = dragon.getPosition().add(offset);
            Tile tile = getBoard().get(position);
            if (tile == null || CountCapability.isTileForbidden(tile)) continue;
            if (dragonVisitedTiles != null && dragonVisitedTiles.contains(position)) { continue; }
            if (fairyCap != null && position.equals(fairyCap.getFairy().getPosition())) { continue; }
            result.add(position);
        }
        return result;
    }



    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (dragon.isDeployed()) {
            Element dragonEl = doc.createElement("dragon");
            XMLUtils.injectPosition(dragonEl, dragon.getPosition());
            if (dragonMovesLeft > 0) {
                dragonEl.setAttribute("moves", "" + dragonMovesLeft);
                dragonEl.setAttribute("movingPlayer", "" + dragonPlayer.getIndex());
                if (dragonVisitedTiles != null) {
                    for (Position visited : dragonVisitedTiles) {
                        Element ve = doc.createElement("visited");
                        XMLUtils.injectPosition(ve, visited);
                        dragonEl.appendChild(ve);
                    }
                }
            }
            node.appendChild(dragonEl);
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("dragon");
        if (nl.getLength() > 0) {
            Element dragonEl = (Element) nl.item(0);
            dragon.deploy(XMLUtils.extractPosition(dragonEl));
            if (dragonEl.hasAttribute("moves")) {
                dragonMovesLeft  = Integer.parseInt(dragonEl.getAttribute("moves"));
                dragonPlayer = game.getPlayer(Integer.parseInt(dragonEl.getAttribute("movingPlayer")));
                dragonVisitedTiles = new HashSet<>();
                NodeList vl = dragonEl.getElementsByTagName("visited");
                for (int i = 0; i < vl.getLength(); i++) {
                    dragonVisitedTiles.add(XMLUtils.extractPosition((Element) vl.item(i)));
                }
            }
        }
    }
    */

}
