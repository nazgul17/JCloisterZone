package com.jcloisterzone.game.capability;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Predicates;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.FairyNextToAction;
import com.jcloisterzone.action.FairyOnTileAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.MeeplePrisonEvent;
import com.jcloisterzone.event.TowerIncreasedEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

/**
 * @model Array<List<String>> - list of captured meeples for each players
 */
public final class TowerCapability extends Capability<Array<List<Meeple>>> {

    private static final int RANSOM_POINTS = 3;

//    private final Set<Position> towers = new HashSet<>();
//    private final Map<Player, Integer> towerPieces = new HashMap<>();
//    private boolean ransomPaidThisTurn;
//
//    private Position lastIncreasedTower; //needed for persist game in TowerCapturePhase

    //key is Player who keeps follower imprisoned
    //synchronized because of GUI is looking inside
    //TODO fix gui hack
    //private final Map<Player, List<Follower>> prisoners = Collections.synchronizedMap(new HashMap<Player, List<Follower>>());

    private int getInitialPiecesCount(GameState state) {
        switch(state.getPlayers().getPlayers().length()) {
        case 1:
        case 2: return 10;
        case 3: return 9;
        case 4: return 7;
        case 5: return 6;
        case 6: return 5;
        }
        throw new IllegalStateException();
    }

    @Override
    public GameState onStartGame(GameState state) {
        int pieces = getInitialPiecesCount(state);
        return state.updatePlayers(ps -> {
            for (Player p : ps.getPlayers()) {
                ps = ps.addPlayerTokenCount(p.getIndex(), Token.TOWER_PIECE, pieces);
            }
            return ps;
        });
    }

        @Override
    public GameState onActionPhaseEntered(GameState state) {
        Player player = state.getPlayerActions().getPlayer();

        Set<FeaturePointer> occupiedTowers = state.getDeployedMeeples()
            .filter(t -> t._2.getLocation().equals(Location.TOWER))
            .map(Tuple2::_2)
            .toSet();

        Stream<Tuple2<FeaturePointer, Feature>> openTowersStream = Stream.ofAll(state.getFeatures())
            .filter(t -> (t._2 instanceof Tower))
            .filter(t -> !occupiedTowers.contains(t._1));

        Set<FeaturePointer> openTowersForPiece = openTowersStream
            .map(Tuple2::_1).toSet();

        Set<FeaturePointer> openTowersForFollower = openTowersStream
            .filter(t -> ((Tower)t._2).getHeight() > 0)
            .map(Tuple2::_1).toSet();

        ActionsState as = state.getPlayerActions();

        if (!openTowersForPiece.isEmpty()) {
            as = as.appendAction(new TowerPieceAction(openTowersForPiece.map(fp -> fp.getPosition())));
        }

        if (!openTowersForFollower.isEmpty()) {
            Vector<Meeple> availMeeples = player.getMeeplesFromSupply(
                state,
                Vector.of(SmallFollower.class, BigFollower.class, Phantom.class)
            );
            Vector<PlayerAction<?>> actions = availMeeples.map(meeple ->
                new MeepleAction(meeple.getClass(), openTowersForFollower)
            );
            as = as.appendActions(actions).mergeMeppleActions();
        }

        return state.setPlayerActions(as);
    }

//    public void decreaseTowerPieces(Player player) {
//        int pieces = getTowerPieces(player);
//        if (pieces == 0) throw new IllegalStateException("Player has no tower pieces");
//        towerPieces.put(player, pieces-1);
//    }
//
//    private boolean hasSmallOrBigFollower(Player p) {
//        return Iterables.any(p.getFollowers(), Predicates.and(
//                MeeplePredicates.inSupply(), MeeplePredicates.instanceOf(SmallFollower.class, BigFollower.class)));
//    }
//
//    @Override
//    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
//        if (hasSmallOrBigFollower(game.getActivePlayer())) {
//            prepareTowerFollowerDeploy(findAndFillFollowerActions(actions));
//        }
//        if (getTowerPieces(game.getActivePlayer()) > 0) {
//            Set<Position> availTowers = getOpenTowers(0);
//            if (!availTowers.isEmpty()) {
//                actions.add(new TowerPieceAction().addAll(availTowers));
//            }
//        }
//    }
//
//    public void prepareTowerFollowerDeploy(List<MeepleAction> followerActions) {
//        Set<Position> availableTowers = getOpenTowers(1);
//        if (!availableTowers.isEmpty()) {
//            for (Position p : availableTowers) {
//                if (game.isDeployAllowed(getBoard().getPlayer(p), Follower.class)) {
//                    for (MeepleAction ma : followerActions) {
//                        //only small, big and phantoms are allowed on top of tower
//                        if (SmallFollower.class.isAssignableFrom(ma.getMeepleType()) || BigFollower.class.isAssignableFrom(ma.getMeepleType())) {
//                            ma.add(new FeaturePointer(p, Location.TOWER));
//                        }
//                    }
//                }
//            }
//        }
//    }

//    public void placeTowerPiece(Player player, Position pos) {
//        Tower tower = getBoard().getPlayer(pos).getTower();
//        if (tower  == null) {
//            throw new IllegalArgumentException("No tower on tile.");
//        }
//        if (tower.getMeeple() != null) {
//            throw new IllegalArgumentException("The tower is sealed");
//        }
//        decreaseTowerPieces(player);
//        tower.increaseHeight();
//        lastIncreasedTower = pos;
//        game.post(new TowerIncreasedEvent(player, pos, tower.getHeight()));
//    }
//
//    protected Set<Position> getOpenTowers(int minHeight) {
//        Set<Position> availTower = new HashSet<>();
//        for (Position p : getTowers()) {
//            Tower t = getBoard().getPlayer(p).getTower();
//            if (t.getMeeple() == null && t.getHeight() >= minHeight) {
//                availTower.add(p);
//            }
//        }
//        return availTower;
//    }

    public Map<Player, List<Follower>> getPrisoners() {
        return prisoners;
    }

    public Position getLastIncreasedTower() {
        return lastIncreasedTower;
    }

    public void setLastIncreasedTower(Position lastIncreasedTower) {
        this.lastIncreasedTower = lastIncreasedTower;
    }

    public boolean hasImprisonedFollower(Player followerOwner) {
        for (Follower m : followerOwner.getFollowers()) {
            if (m.isInPrison()) return true;
        }
        return false;
    }

    public boolean hasImprisonedFollower(Player followerOwner, Class<? extends Follower> followerClass) {
        for (Follower m : followerOwner.getFollowers()) {
            if (m.isInPrison() && m.getClass().equals(followerClass)) return true;
        }
        return false;
    }

    public void inprison(Follower m, Player player) {
        assert m.getLocation() == null;
        prisoners.get(player).add(m);
        game.post(new MeeplePrisonEvent(m, null, player));
        m.setInPrison(true);
    }

    public void payRansom(Integer playerIndexToPay, Class<? extends Follower> meepleType) {
        if (ransomPaidThisTurn) {
            throw new IllegalStateException("Ransom alreasy paid this turn");
        }
        Player opponent = game.getAllPlayers().getPlayer(playerIndexToPay);

        Iterator<Follower> i = prisoners.get(opponent).iterator();
        while (i.hasNext()) {
            Follower meeple = i.next();
            if (meepleType.isInstance(meeple)) {
                i.remove();
                meeple.setInPrison(false);
                opponent.addPoints(RANSOM_POINTS, PointCategory.TOWER_RANSOM);
                ransomPaidThisTurn = true;
                game.getActivePlayer().addPoints(-RANSOM_POINTS, PointCategory.TOWER_RANSOM);
                game.post(new MeeplePrisonEvent(meeple, opponent, null));
                game.getPhase().notifyRansomPaid();
                return;
            }
        }
        throw new IllegalStateException("Opponent has no figure to exchage");
    }
}
