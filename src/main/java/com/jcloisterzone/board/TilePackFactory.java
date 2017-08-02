package com.jcloisterzone.board;

import static com.jcloisterzone.XMLUtils.attributeIntValue;
import static com.jcloisterzone.XMLUtils.attributeStringValue;
import static com.jcloisterzone.XMLUtils.getTileId;

import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;


public class TilePackFactory {

    public static class Tiles {
        private final TilePackState tilePack;
        private List<Tuple2<TileDefinition, Position>> preplacedTiles;

        public Tiles(TilePackState tilePack, List<Tuple2<TileDefinition, Position>> preplacedTiles) {
            super();
            this.tilePack = tilePack;
            this.preplacedTiles = preplacedTiles;
        }

        public TilePackState getTilePack() {
            return tilePack;
        }

        public List<Tuple2<TileDefinition, Position>> getPreplacedTiles() {
            return preplacedTiles;
        }
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_TILE_GROUP = "default";

    private final TileDefinitionBuilder tileFactory = new TileDefinitionBuilder();

    protected GameState state;
    protected Set<Expansion> expansions;
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


    public void setGameState(GameState state) {
        this.state = state;
        tileFactory.setGameState(state);
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setExpansions(Set<Expansion> expansions) {
        this.expansions = expansions;
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
            (
                state.getCapabilities().hasCapability(TunnelCapability.class) &&
                state.getBooleanValue(CustomRule.TUNNELIZE_ALL_EXPANSIONS)
            );
    }

    protected int getTileCount(Element card, String tileId) {
        if (TileDefinition.ABBEY_TILE_ID.equals(tileId)) {
            return PlayerSlot.COUNT;
        } else {
            return attributeIntValue(card, "count", 1);
        }
    }

    protected String getTileGroup(TileDefinition tile, Element card) {
        for (Capability<?> cap: state.getCapabilities().toSeq()) {
            String group = cap.getTileGroup(tile);
            if (group != null) return group;
        }
        return attributeStringValue(card, "group", DEFAULT_TILE_GROUP);
    }

    public TileDefinition initTile(TileDefinition tile, Element xml) {
        for (Capability<?> cap: state.getCapabilities().toSeq()) {
            tile = cap.initTile(tile, xml);
        }
        return tile;
    }

    public TileDefinition createTileDefinition(Expansion expansion, String tileId, Element tileElement) {
        if (usedIds.contains(tileId)) {
            throw new IllegalArgumentException("Multiple occurences of id " + tileId + " in tile definition xml.");
        }
        usedIds.add(tileId);

        TileDefinition tileDef = tileFactory.createTile(expansion, tileId, tileElement, isTunnelActive(expansion));
        try {
            tileDef = initTile(tileDef, tileElement);
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

    public Tiles createTilePack() {
        Map<String, Integer> discardList = getDiscardTiles();

        defs.forEach((expansion, element) -> {
            NodeList nl = element.getElementsByTagName("tile");
            XMLUtils.elementStream(nl).forEach(tileElement -> {

                if (!state.getCapabilities().hasCapability(RiverCapability.class)) {
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
                        if (expansions.contains(Expansion.COUNT)) {
                            if (tileId.equals("BA.RCr")) continue;
                            if (tileId.equals("R1.I.s") ||
                                tileId.equals("R2.I.s") ||
                                tileId.equals("GQ.RFI")) {
                                pos = new Position(1, 2);
                            }
                            if (tileId.equals("WR.CFR")) {
                                pos = new Position(-2, -2);
                            }
                        } else if (expansions.contains(Expansion.WIND_ROSE)) {
                            if (tileId.equals("BA.RCr")) continue;
                            if (state.getCapabilities().hasCapability(RiverCapability.class)) {
                                if (tileId.equals("WR.CFR")) {
                                    pos = new Position(0, 1);
                                }
                            }
                        }
                        logger.info("Setting initial placement {} for {}", pos, tileId);
                    }
                    if (pos != null) {
                        //TODO groups need to be applied
                        //some preplaced are not used?
                        //IMMUTABLE TODO
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
        return new Tiles(
            new TilePackState(groups),
            List.ofAll(preplacedTiles)
        );
    }
}
