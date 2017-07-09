package com.jcloisterzone.ui.grid.layer;

import static com.jcloisterzone.ui.I18nUtils._;

import javax.swing.JOptionPane;

import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.wsio.message.DeployFlierMessage;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;


public class FeatureAreaLayer extends AbstractAreaLayer {

    private Set<Position> abbotOption = HashSet.empty();
    private Set<Position> abbotOnlyOption = HashSet.empty();

    public FeatureAreaLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public SelectFeatureAction getAction() {
        return (SelectFeatureAction) super.getAction();
    }

    @Override
    protected Map<BoardPointer, FeatureArea> prepareAreas() {
        SelectFeatureAction action = getAction();
        Map<BoardPointer, FeatureArea> result = HashMap.empty();
        abbotOption = HashSet.empty();
        abbotOnlyOption = HashSet.empty();

        for (Tuple2<Position, Set<Location>> t : action.groupByPosition()) {
            Position pos = t._1;
            Set<Location> locations = t._2;

            if (locations.contains(Location.ABBOT)) {
                abbotOption = abbotOption.add(pos);
                if (!locations.contains(Location.CLOISTER)) {
                    locations.add(Location.CLOISTER);
                    abbotOnlyOption = abbotOnlyOption.add(pos);
                }
                locations.remove(Location.ABBOT);
            }

            Tile tile = gridPanel.getTile(pos);
            int sizeX, sizeY;
            if (tile.getRotation() == Rotation.R0 || tile.getRotation() == Rotation.R180) {
                sizeX = getTileWidth();
                sizeY = getTileHeight();
            } else {
                sizeY = getTileWidth();
                sizeX = getTileHeight();
            }

            Map<Location, FeatureArea> locMap;
            if (action instanceof BridgeAction) {
                locMap = rm.getBridgeAreas(tile, sizeX, sizeY, locations);
            } else {
                locMap = rm.getFeatureAreas(tile, sizeX, sizeY, locations);
            }
            result = addAreasToResult(result, locMap, pos, sizeX, sizeY);
        }

        return result;
    }


    @Override
    protected void performAction(BoardPointer ptr) {
        SelectFeatureAction action = getAction();
        FeaturePointer fp = (FeaturePointer) ptr;
        if (action instanceof MeepleAction) {
            MeepleAction ma = (MeepleAction) action;

            if (fp.getLocation() == Location.FLIER) {
                getClient().getConnection().send(new DeployFlierMessage(getGame().getGameId(), ma.getMeepleType()));
                return;
            }
            if (fp.getLocation() == Location.CLOISTER && abbotOption.contains(fp.getPosition())) {
                String[] options;
                boolean abbotOnlyOptionValue = abbotOption.contains(fp.getPosition());
                if (abbotOnlyOptionValue) {
                    options = new String[] {_("Place as abbot")};
                } else {
                    options = new String[] {_("Place as monk"), _("Place as abbot") };
                }
                int result = JOptionPane.showOptionDialog(getClient(),
                    _("How do you want to place follower on monastery?"),
                    _("Monastery"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (result == -1) { //closed dialog
                    return;
                }
                if (abbotOnlyOptionValue || result == JOptionPane.NO_OPTION) {
                    fp = new FeaturePointer(fp.getPosition(), Location.ABBOT);
                }
            }
        }
        action.perform(getRmiProxy(), fp);
        return;
    }


}
