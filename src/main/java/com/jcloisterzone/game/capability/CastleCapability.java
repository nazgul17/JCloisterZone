package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;

public class CastleCapability extends Capability<Void> {

    @Override
    public GameState onStartGame(GameState state) {
        int tokens = state.getPlayers().length() < 5 ? 3 : 2;
        state = state.updatePlayers(ps -> {
            for (Player p : ps.getPlayers()) {
                ps = ps.addPlayerTokenCount(p.getIndex(), Token.CASTLE, tokens);
            }
            return ps;
        });
        return state;
    }

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof City) {
            feature = ((City) feature).setCastleBase(attributeBoolValue(xml, "castle-base"));
        }
        return feature;
    }

//    private final Map<Player, Integer> castles = new HashMap<>();
//
//    private Player castlePlayer;
//    private Map<Player, Set<Location>> currentTileCastleBases = null;
//
//    /** castles deployed this turn - cannot be scored - refs to master feature  */
//    private final List<Castle> newCastles = new ArrayList<>();
//    /** empty castles, already scored, keeping ref for game save */
//    private final List<Castle> emptyCastles = new ArrayList<>();
//    /** castles from previous turns, can be scored - castle -> vinicity area */
//    private final Map<Castle, Position[]> scoreableCastleVicinity = new HashMap<>();
//    private final Map<Castle, Integer> castleScore = new HashMap<>();
//
//    @Override
//    public void handleEvent(PlayEvent event) {
//       if (event instanceof MeepleEvent) {
//           undeployed((MeepleEvent) event);
//       }
//
//    }
//
//    private void undeployed(MeepleEvent ev) {
//        if (ev.getFrom() == null) return;
//        Feature f = getBoard().getPlayer(ev.getFrom());
//        if (f instanceof Castle) {
//            Castle castle = (Castle) f.getMaster();
//            scoreableCastleVicinity.remove(castle);
//            emptyCastles.add(castle);
//        }
//    }
//
//    @Override
//    public void initPlayer(Player player) {
//        int players = game.getAllPlayers().length();
//        if (players < 5) {
//            castles.put(player, 3);
//        } else {
//            castles.put(player, 2);
//        }
//    }
//
//    @Override
//    public Feature initFeature(GameSettings gs, String tileId, Feature feature, Element xml) {
//        if (feature instanceof City) {
//            ((City) feature).setCastleBase(attributeBoolValue(xml, "castle-base"));
//        }
//    }
//
//    private void checkCastleVicinity(Iterable<Position> triggerPositions, int score) {
//        for (Position p : triggerPositions) {
//            for (Entry<Castle, Position[]> entry : scoreableCastleVicinity.entrySet()) {
//                Position[] vicinity = entry.getValue();
//                for (int i = 0; i < vicinity.length; i++) {
//                    if (vicinity[i].equals(p)) {
//                        Castle master = entry.getKey();
//                        Integer currentCastleScore = castleScore.get(master);
//                        if (currentCastleScore == null || currentCastleScore < score) {
//                            castleScore.put(master, score);
//                            //chain reaction, one completed castle triggers another
//                            checkCastleVicinity(Arrays.asList(master.getCastleBase()), score);
//                        }
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//    private Castle replaceCityWithCastle(Tile tile, Location loc) {
//        ListIterator<Feature> iter = tile.getFeatures().listIterator();
//        City city = null;
//        while (iter.hasNext()) {
//            Feature feature =  iter.next();
//            if (feature.getLocation() == loc) {
//                city = (City) feature;
//                break;
//            }
//        }
//        List<Meeple> meeples = new ArrayList<>(city.getMeeples()); //collection copy required!!! undeploy modify it
//        for (Meeple m : meeples) {
//            m.undeploy();
//        }
//        Castle castle = new Castle();
//        castle.setTile(tile);
//        castle.setId(game.idSequnceNextVal());
//        castle.setLocation(loc.rotateCCW(tile.getRotation()));
//        iter.set(castle);
//
//        for (Feature f : tile.getFeatures()) { //replace also city references
//            if (f instanceof Farm) {
//                Farm farm = (Farm) f;
//                Feature[] adjoining = farm.getAdjoiningCities();
//                if (adjoining != null) {
//                    for (int i = 0; i < adjoining.length; i++) {
//                        if (adjoining[i] == city) {
//                            adjoining[i] = castle;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        FeaturePointer fp = new FeaturePointer(tile.getPosition(), loc);
//        for (Meeple m : meeples) {
//            if (m.getPlayer() == game.getActivePlayer() && m.isDeploymentAllowed(castle).result) {
//                m.deploy(fp);
//            }
//        }
//        return castle;
//    }
//
//    public Castle convertCityToCastle(Position pos, Location loc) {
//        return convertCityToCastle(pos, loc, false);
//    }
//
//    private Castle convertCityToCastle(Position pos, Location loc, boolean loadFromSnaphot) {
//        Castle castle1 = replaceCityWithCastle(getBoard().getPlayer(pos), loc);
//        Castle castle2 = replaceCityWithCastle(getBoard().getPlayer(pos.add(loc)), loc.rev());
//        castle1.getEdges()[0] = castle2;
//        castle2.getEdges()[0] = castle1;
//        if (!loadFromSnaphot) {
//            newCastles.add(castle1.getMaster());
//        }
//        game.post(new CastleDeployedEvent(game.getActivePlayer(), castle1, castle2));
//        return castle1.getMaster();
//    }
//
//    @Override
//    public GameState onCompleted(GameState state, Completable ctx) {
//        checkCastleVicinity(ctx.getPositions(), ctx.getPoints());
//    }
//
//    public Map<Castle, Integer> getCastleScore() {
//        return castleScore;
//    }
//
//    @Override
//    public void turnPartCleanUp() {
//        for (Castle castle: newCastles) {
//            scoreableCastleVicinity.put(castle, castle.getVicinity());
//        }
//        newCastles.clear();
//        castleScore.clear();
//    }
//
//    public Player getCastlePlayer() {
//        return castlePlayer;
//    }
//
//    public void setCastlePlayer(Player castlePlayer) {
//        this.castlePlayer = castlePlayer;
//    }
//
//    public Map<Player, Set<Location>> getCurrentTileCastleBases() {
//        return currentTileCastleBases;
//    }
//
//    public void setCurrentTileCastleBases(Map<Player, Set<Location>> currentTileCastleBases) {
//        this.currentTileCastleBases = currentTileCastleBases;
//    }
//
//
//    public int getPlayerCastles(Player pl) {
//        return castles.get(pl);
//    }
//
//
//    public void decreaseCastles(Player player) {
//        int n = getPlayerCastles(player);
//        if (n == 0) throw new IllegalStateException("Player has no castles");
//        castles.put(player, n-1);
//    }
}
