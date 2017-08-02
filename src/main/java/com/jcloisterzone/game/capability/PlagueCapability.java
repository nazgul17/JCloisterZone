package com.jcloisterzone.game.capability;

import com.jcloisterzone.game.Capability;

public class PlagueCapability extends Capability<Void> {

    /*
    private final List<PlagueSource> plagueSources = new ArrayList<>(6);

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("plague").getLength() > 0) {
            tile.setTrigger(TileTrigger.PLAGUE);
        }
    }

    @Override
    public String getTileGroup(Tile tile) {
        return tile.hasTrigger(TileTrigger.PLAGUE) ? "plague" : null;
    }

    @Override
    public void begin() {
        //TODO replace with activation after 17th tile drawn
        getTilePack().setGroupState("plague", TileGroupState.ACTIVE);
    }

    @Override
    public boolean isDeployAllowed(GameState state, Position pos) {
        for (PlagueSource ps : plagueSources) {
            if (ps.active && ps.pos.equals(pos)) return false;
        }
        //TODO check flea locations
        return true;
    }

    public List<PlagueSource> getPlagueSources() {
        return plagueSources;
    }

    public List<Position> getActiveSources() {
        List<Position> result = new ArrayList<>(6);
        for (PlagueSource source : plagueSources) {
            if (source.active) {
                result.add(source.pos);
            }
        }
        return result;
    }

    public static class PlagueSource {
        public Position pos;
        public boolean active = true;

        public PlagueSource(Position pos) {
            this.pos = pos;
        }
    }
    */
}
