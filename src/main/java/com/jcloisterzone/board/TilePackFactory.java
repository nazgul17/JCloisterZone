package com.jcloisterzone.board;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.game.capability.TunnelCapability;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;

import static com.jcloisterzone.XMLUtils.attributeIntValue;
import static com.jcloisterzone.XMLUtils.attributeStringValue;
import static com.jcloisterzone.XMLUtils.getTileId;


public class TilePackFactory {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_TILE_GROUP = "default";

    private final TileDefinitionBuilder tileFactory = new TileDefinitionBuilder();

    protected Game game;
    protected Config config;
    protected Map<Expansion, Element> defs;

    private java.util.Set<String> usedIds = new java.util.HashSet<>(); //for assertion only

    private java.util.Map<String, java.util.List<TileDefinition>> tiles = new java.util.HashMap<>();
    private java.util.List<Tuple2<TileDefinition, Position>> preplacedTiles = new java.util.ArrayList<>();

    public static class TileCount {
        public String tileId;
        public Integer count;

        public TileCount(String tileId, Integer count) {
            this.tileId = tileId;
            this.count = count;
        }
    }


    public void setGame(Game game) {
        this.game = game;
        tileFactory.setGame(game);
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setExpansions(Iterable<Expansion> expansions) {
        defs = Stream.ofAll(expansions).map(
            exp -> new Tuple2<Expansion, Element>(exp, getExpansionDefinition(exp))
        ).collect(LinkedHashMap.collector());
    }



    public Stream<TileCount> getExpansionTiles(Expansion expansion) {
        Element el = getExpansionDefinition(expansion);
        return XMLUtils.elementStream(el.getElementsByTagName("tile")).map(tileElement -> {
            String tileId = getTileId(expansion, tileElement);
            if (TileDefinition.ABBEY_TILE_ID.equals(tileId)) {
                return new TileCount(tileId, null);
            } else {
                return new TileCount(tileId, getTileCount(tileElement, tileId));
            }
        });
    }

    public int getExpansionSize(Expansion expansion) {
        Element el = getExpansionDefinition(expansion);
        NodeList nl = el.getElementsByTagName("tile");
        int size = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            Element tileElement = (Element) nl.item(i);
            String tileId = getTileId(expansion, tileElement);
            if (!TileDefinition.ABBEY_TILE_ID.equals(tileId)) {
                size += getTileCount(tileElement, tileId);
            }
        }
        return size;
    }

    protected  URL getStandardCardsConfig(Expansion expansion) {
        String fileName = "tile-definitions/"+expansion.name().toLowerCase()+".xml";
        return TilePackFactory.class.getClassLoader().getResource(fileName);
    }

    protected URL getCardsConfig(Expansion expansion) {
        DebugConfig debugConfig = config.getDebug();
        String fileName = null;
        if (debugConfig != null && debugConfig.getTile_definitions() != null) {
            fileName = debugConfig.getTile_definitions().get(expansion.name());
        }
        if (fileName == null) {
            return getStandardCardsConfig(expansion);
        } else {
            return TilePackFactory.class.getClassLoader().getResource(fileName);
        }
    }

    protected Element getExpansionDefinition(Expansion expansion) {
        return XMLUtils.parseDocument(getCardsConfig(expansion)).getDocumentElement();
    }

    protected Map<String, Integer> getDiscardTiles() {
        java.util.Map<String, Integer> discard = new java.util.HashMap<>();
        for (Element expansionDef: defs.values()) {
            NodeList nl = expansionDef.getElementsByTagName("discard");
            XMLUtils.elementStream(nl).forEach(el -> {
                String tileId = el.getAttribute("tile");
                if (discard.containsKey(tileId)) {
                    discard.put(tileId, 1 + discard.get(tileId));
                } else {
                    discard.put(tileId, 1);
                }
            });
        }
        return HashMap.ofAll(discard);
    }

    protected boolean isTunnelActive(Expansion expansion) {
        return expansion == Expansion.TUNNEL ||
            (game.hasCapability(TunnelCapability.class) && game.getBooleanValue(CustomRule.TUNNELIZE_ALL_EXPANSIONS));
    }

    protected int getTileCount(Element card, String tileId) {
        if (TileDefinition.ABBEY_TILE_ID.equals(tileId)) {
            return PlayerSlot.COUNT;
        } else {
            return attributeIntValue(card, "count", 1);
        }
    }

    protected String getTileGroup(TileDefinition tile, Element card) {
        String group = game.getTileGroup(tile);
        if (group != null) return group;
        return attributeStringValue(card, "group", DEFAULT_TILE_GROUP);
    }

    public TileDefinition createTileDefinition(Expansion expansion, String tileId, Element tileElement) {
        if (usedIds.contains(tileId)) {
            throw new IllegalArgumentException("Multiple occurences of id " + tileId + " in tile definition xml.");
        }
        usedIds.add(tileId);

        TileDefinition tileDef = tileFactory.createTile(expansion, tileId, tileElement, isTunnelActive(expansion));
        try {
            tileDef = game.initTile(tileDef, tileElement);
        } catch (RemoveTileException ex) {
            return null;
        }

        return tileDef;
    }

    public Stream<Position> getPreplacedPositions(String tileId, Element card) {
        NodeList nl = card.getElementsByTagName("position");
        return XMLUtils.elementStream(nl).map(
            e -> new Position(attributeIntValue(e, "x"), attributeIntValue(e, "y"))
        );
    }

    public Tuple2<TilePack, List<Tuple2<TileDefinition, Position>>> createTilePack() {
        Map<String, Integer> discardList = getDiscardTiles();

        defs.forEach((expansion, element) -> {
            NodeList nl = element.getElementsByTagName("tile");
            XMLUtils.elementStream(nl).forEach(tileElement -> {

                if (!game.hasCapability(RiverCapability.class)) {
                    //skip river tiles if not playing River to prevent wrong tile count in pack (GQ11 rivers)
                    if (tileElement.getElementsByTagName("river").getLength() > 0) {
                        return;
                    }
                }

                String tileId = getTileId(expansion, tileElement);
                List<Position> positions = getPreplacedPositions(tileId, tileElement).toList();
                int count = getTileCount(tileElement, tileId);
                int n = discardList.get(tileId).getOrElse(0);

                count -= n;
                if (count <= 0) { //discard can be in multiple expansions and than can be negative
                    return;
                }

                TileDefinition tileDef = createTileDefinition(expansion, tileId, tileElement);
                if (tileDef == null) {
                    return;
                }

                for (int ci = 0; ci < count; ci++) {
                    Position pos = null;
                    if (positions != null && !positions.isEmpty()) {
                        pos = positions.peek();
                        positions = positions.pop();
                        //hard coded exceptions - should be declared in pack def
                        if (game.hasExpansion(Expansion.COUNT)) {
                            if (tileId.equals("BA.RCr")) continue;
                            if (tileId.equals("R1.I.s") ||
                                tileId.equals("R2.I.s") ||
                                tileId.equals("GQ.RFI")) {
                                pos = new Position(1, 2);
                            }
                            if (tileId.equals("WR.CFR")) {
                                pos = new Position(-2, -2);
                            }
                        } else if (game.hasExpansion(Expansion.WIND_ROSE)) {
                            if (tileId.equals("BA.RCr")) continue;
                            if (game.hasCapability(RiverCapability.class)) {
                                if (tileId.equals("WR.CFR")) {
                                    pos = new Position(0, 1);
                                }
                            }
                        }
                        logger.info("Setting initial placement {} for {}", pos, tileId);
                    }
                    if (pos != null) {
                        preplacedTiles.add(new Tuple2<>(tileDef, pos));
                    } else {
                        String group = getTileGroup(tileDef, tileElement);
                        if (!tiles.containsKey(group)) {
                            tiles.put(group, new java.util.ArrayList<>());
                        }
                        tiles.get(group).add(tileDef);
                    }
                }
            });
        });

        Map<String, Array<TileDefinition>> groups = HashMap.ofAll(tiles).mapValues(l -> Array.ofAll(l));
        return new Tuple2<>(
            new DefaultTilePack(groups),
            List.ofAll(preplacedTiles)
        );
    }

    public Game getGame() {
        return game;
    }

}
