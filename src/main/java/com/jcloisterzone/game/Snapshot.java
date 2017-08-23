package com.jcloisterzone.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.VersionComparator;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.LoadGameTilePackFactory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.phase.Phase;

@Deprecated
public class Snapshot implements Serializable {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String COMPATIBLE_FROM = "2.6"; //on incompatible change, remove alsi !capabilityName.startsWith("com.") code

    private Document doc;
    private Element root;

    private Map<Position, Element> tileElemens; //write cache

    private boolean gzipOutput = true;


    public Snapshot(Game game) {
        createRootStructure(game);
        createRuleElements(game);
        createExpansionElements(game);
        createCapabilityElements(game);
        createPlayerElements(game);
        createTileElements(game);
        createMeepleElements(game);
    }

    public Snapshot(File savedGame) throws IOException, SAXException {
        try {
            load(new GZIPInputStream(new FileInputStream(savedGame)));
        } catch (IOException e) {
            if ("Not in GZIP format".equals(e.getMessage())) {
                load(new FileInputStream(savedGame));
            } else {
                throw e;
            }
        }
    }

    public Snapshot(String snapshot) throws IOException {
        load(snapshot);
    }

    public boolean isGzipOutput() {
        return gzipOutput;
    }

    public void setGzipOutput(boolean gzipOutput) {
        this.gzipOutput = gzipOutput;
    }


    private void createRootStructure(Game game) {
        doc = XMLUtils.newDocument();
        root = doc.createElement("game");
        root.setAttribute("app-version", Application.VERSION);
        root.setAttribute("phase", game.getPhase().getClass().getName());
        root.setAttribute("seed", ""+game.getRandomSeed());
        doc.appendChild(root);

    }

    private void createRuleElements(Game game) {
        for (Entry<CustomRule, Object> entry : game.getCustomRules().entrySet()) {
            CustomRule cr = entry.getKey();
            if (cr.equals(CustomRule.RANDOM_SEATING_ORDER)) continue;
            Element el = doc.createElement("rule");
            el.setAttribute("name", cr.name());
            el.setAttribute("value", entry.getValue().toString());
            root.appendChild(el);
        }
    }

    private void createExpansionElements(Game game) {
        for (Expansion exp : game.getExpansions()) {
            Element el = doc.createElement("expansion");
            el.setAttribute("name", exp.name());
            root.appendChild(el);
        }
    }

    private void createCapabilityElements(Game game) {
        for (Capability cap : game.getCapabilities()) {
            Element el = doc.createElement("capability");
            el.setAttribute("name", cap.getClass().getSimpleName().replace("Capability", ""));
            root.appendChild(el);
            cap.saveToSnapshot(doc, el);
        }
    }


    private void createPlayerElements(Game game) {
        Element parent = doc.createElement("players");
        parent.setAttribute("turn", "" + game.getTurnPlayer().getIndex());
        root.appendChild(parent);
        for (Player p : game.getAllPlayers()) {
            Element el = doc.createElement("player");
            el.setAttribute("name", p.getNick());
            el.setAttribute("points", "" + p.getPoints());
            el.setAttribute("slot", "" + p.getSlot().getNumber());
            el.setAttribute("clientId", p.getSlot().getClientId());
            if (p.getSlot().isAi()) {
                el.setAttribute("ai-class", p.getSlot().getAiClassName());
            }
            for (PointCategory cat : PointCategory.values()) {
                int points = p.getPointsInCategory(cat);
                if (points != 0) { //can be <0 (ransom)
                    Element catEl = doc.createElement("point-category");
                    catEl.setAttribute("name", cat.name());
                    catEl.setAttribute("points", "" + points);
                    el.appendChild(catEl);
                }
            }
            Element clockEl = doc.createElement("clock");
            clockEl.setAttribute("time", ""+p.getClock().getTime());
            if (p.getClock().isRunning()) {
                clockEl.setAttribute("running", "true");
            }
            el.appendChild(clockEl);
            parent.appendChild(el);
        }
    }

    private void createTileElements(Game game) {
        tileElemens = new HashMap<Position, Element>();
        Element parent = doc.createElement("tiles");
        if (game.getCurrentTile() != null) {
            parent.setAttribute("next", game.getCurrentTile().getId());
        }
        root.appendChild(parent);
        for (String group : game.getTilePack().getGroups()) {
            if (group.equals(LoadGameTilePackFactory.PLACED_GROUP)) continue; //empty technical group created only when loading game
            if (group.equals(TilePack.INACTIVE_GROUP)) continue; //system always existing group
            Element el = doc.createElement("group");
            el.setAttribute("name", group);
            el.setAttribute("state", game.getTilePack().getGroupState(group).name());
            parent.appendChild(el);
        }
        for (Tile tile : game.getBoard().getAllTiles()) {
            Element el = doc.createElement("tile");
            el.setAttribute("name", tile.getId());
            el.setAttribute("rotation", tile.getRotation().name());
            XMLUtils.injectPosition(el, tile.getPosition());
            parent.appendChild(el);
            tileElemens.put(tile.getPosition(), el);
            game.saveTileToSnapshot(tile, doc, el);
        }
        for (Tile tile : game.getBoard().getDiscardedTiles()) {
            Element el = doc.createElement("discard");
            el.setAttribute("name", tile.getId());
            parent.appendChild(el);
        }
    }

    private void createMeepleElements(Game game) {
        for (Meeple m : game.getDeployedMeeples()) {
            Element tileEl = tileElemens.getPlayer(m.getPosition());
            Element el = doc.createElement("meeple");
            el.setAttribute("player", "" + m.getPlayer().getIndex());
            el.setAttribute("type", "" + m.getClass().getSimpleName());
            el.setAttribute("meeple-id", "" + m.getId());
            el.setAttribute("loc", "" + m.getLocation());
            tileEl.appendChild(el);
        }
    }

    public void save(OutputStream os) throws TransformerException, IOException {
        save(os, gzipOutput);
    }

    public void save(OutputStream os, boolean gzipOutput) throws TransformerException, IOException {
        save(os, gzipOutput, true);
    }

    //TODO move close on caller
    public void save(OutputStream os, boolean gzipOutput, boolean close) throws TransformerException, IOException {
        StreamResult streamResult;
        if (gzipOutput) {
            streamResult = new StreamResult(new GZIPOutputStream(os));
        } else {
            streamResult = new StreamResult(os);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        serializer.setOutputProperty(OutputKeys.METHOD, "xml");
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
        serializer.transform(new DOMSource(doc), streamResult);
        if (close) {
            streamResult.getOutputStream().close();
        }
    }

    public String saveToString() throws TransformerException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        save(baos, false);
        baos.close();
        return new String(baos.toByteArray(),"UTF-8");
    }

    public void load(InputStream is) throws SnapshotCorruptedException {
        doc = XMLUtils.parseDocument(is);
        root = doc.getDocumentElement();
        String snapshotVersion = root.getAttribute("app-version");
        if (!snapshotVersion.equals(Application.VERSION) && !snapshotVersion.equals(Application.DEV_VERSION)) {
            if ((new VersionComparator()).compare(snapshotVersion, Snapshot.COMPATIBLE_FROM) < 0) {
                throw new SnapshotVersionException("Saved game is not compatible with current JCloisterZone application. (saved in "+snapshotVersion+")");
            }
        }
    }

    public void load(String s) throws SnapshotCorruptedException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes("UTF-8"));
        load(bais);
        bais.close();
    }

    private NodeList getSecondLevelElelents(String first, String second) {
        return ((Element)root.getElementsByTagName(first).item(0)).getElementsByTagName(second);
    }

    public Set<Expansion> getExpansions() {
        Set<Expansion> result = new HashSet<>();
        NodeList nl = root.getElementsByTagName("expansion");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            Expansion exp = Expansion.valueOf(el.getAttribute("name"));
            result.add(exp);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public void loadCapabilities(Game game) {
        NodeList nl = root.getElementsByTagName("capability");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String capabilityName = el.getAttribute("name");
            if (!capabilityName.startsWith("com.")) { //else 2.X loaded game
                capabilityName = "com.jcloisterzone.game.capability." + el.getAttribute("name") + "Capability";
            }

            //TODO instances should be created here, not in load phase
            Class<? extends Capability> capabilityClass = (Class<? extends Capability>) XMLUtils.classForName(capabilityName);
            Capability capability = game.get(capabilityClass);
            capability.loadFromSnapshot(doc, el);
        }
    }

    public EnumMap<CustomRule, Object> getCustomRules() {
        EnumMap<CustomRule, Object> result = new EnumMap<>(CustomRule.class);
        NodeList nl = root.getElementsByTagName("rule");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            CustomRule rule = CustomRule.valueOf(el.getAttribute("name"));
            String value = el.getAttribute("value");
            if (value == null) {
                //backward compatibility 3.1.x
                result.put(rule, Boolean.TRUE);
            } else {
                result.put(rule, rule.unpackValue(value));
            }
        }
        return result;
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        NodeList nl = getSecondLevelElelents("players", "player");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            PlayerSlot slot = new PlayerSlot(Integer.parseInt(el.getAttribute("slot")));
            Player p = new Player(el.getAttribute("name"), i, slot);
            p.setPoints(Integer.parseInt(el.getAttribute("points")));
            if (el.hasAttribute("ai-class")) {
                String aiClassName = el.getAttribute("ai-class");
                slot.setAiClassName(aiClassName);
            } else {
                if (el.hasAttribute("clientId")) {
                    slot.setClientId(el.getAttribute("clientId"));
                }
            }
            NodeList categories = el.getElementsByTagName("point-category");
            for (int j = 0; j < categories.getLength(); j++) {
                Element catEl = (Element) categories.item(j);
                PointCategory cat = PointCategory.valueOf(catEl.getAttribute("name"));
                p.setPointsInCategory(cat, Integer.parseInt(catEl.getAttribute("points")));
            }
            NodeList clockNl = el.getElementsByTagName("clock");
            if (clockNl.getLength() > 0) {
                Element clockEl = (Element) clockNl.item(0);
                p.getClock().setTime(Long.parseLong(clockEl.getAttribute("time")));
                if (XMLUtils.attributeBoolValue(el, "running")) {
                    p.getClock().setRunning(true);
                }
            }

            players.add(p);
        }
        return players;
    }

    public int getTurnPlayer() {
        Element el = (Element) doc.getElementsByTagName("players").item(0);
        return Integer.parseInt(el.getAttribute("turn"));
    }

    public Map<String, TileGroupState> getActiveGroups() {
        Map<String, TileGroupState> result = new HashMap<>();
        NodeList nl = getSecondLevelElelents("tiles", "group");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            TileGroupState state = TileGroupState.valueOf(el.getAttribute("state"));
            result.put(el.getAttribute("name"), state);
        }
        return result;
    }

    public List<String> getDiscardedTiles() {
        List<String> result = new ArrayList<>();
        NodeList nl = getSecondLevelElelents("tiles", "discard");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            result.add(el.getAttribute("name"));
        }
        return result;
    }

    public Rotation extractTileRotation(Element el) {
        return Rotation.valueOf(el.getAttribute("rotation"));
    }

    @SuppressWarnings("unchecked")
    public List<Meeple> extractTileMeeples(Element tileEl, Game game, Position pos) throws SnapshotCorruptedException {
        NodeList nl = tileEl.getElementsByTagName("meeple");
        List<Meeple> result = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            Location loc = Location.valueOf(el.getAttribute("loc"));
            int playerIndex = Integer.parseInt(el.getAttribute("player"));
            Player player = game.getPlayer(playerIndex);

            Meeple meeple = null;
            if (el.hasAttribute("meeple-id")) {
                String meepleId = el.getAttribute("meeple-id");
                for (Meeple m : player.getMeeples()) {
                    if (m.getId().equals(meepleId)) {
                        meeple = m;
                        break;
                    }
                }
            } else {
                //compatibility with 3.2.0
                String meepleType = el.getAttribute("type");
                if (!meepleType.startsWith("com.")) { // 2.X snapshot compatibility
                    meepleType = "com.jcloisterzone.figure." + meepleType;
                }
                Class<? extends Meeple> mt = (Class<? extends Meeple>) XMLUtils.classForName(meepleType);

                meeple = player.getMeepleFromSupply(mt);
            }
            meeple.setFeaturePointer(new FeaturePointer(pos, loc));
            //don't set feature here. Feature must be set after meeple deployment to correct replace ref during merge
            result.add(meeple);
        }
        return result;
    }

    public NodeList getTileElements() {
        return getSecondLevelElelents("tiles", "tile");
    }

    public String getNextTile() {
        Element el = (Element) root.getElementsByTagName("tiles").item(0);
        return el.getAttribute("next");
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Phase> getActivePhase() throws SnapshotCorruptedException {
        return (Class<? extends Phase>) XMLUtils.classForName(root.getAttribute("phase"));
    }

    public Game asGame(String gameId) {
        long seed = Long.parseLong(root.getAttribute("seed"));
        return asGame(new Game(gameId, seed));
    }

    public Game asGame(Game game) {
        game.getExpansions().clear();
        game.getExpansions().addAll(getExpansions());
        game.getCustomRules().clear();
        game.getCustomRules().putAll(getCustomRules());
        game.setPlayers(getPlayers(), getTurnPlayer());
        return game;
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        String xml = (String) stream.readObject();
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        load(is);
        logger = LoggerFactory.getLogger(getClass());
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //StreamResult streamResult = new StreamResult(new ZipOutputStream(os));
        StreamResult streamResult = new StreamResult(os);
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer serializer = tf.newTransformer();
            serializer.transform(new DOMSource(doc), streamResult);
            stream.writeObject(os.toString());
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

}
