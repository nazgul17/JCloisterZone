package com.jcloisterzone.game.capability;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.FeatureCompletedEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.score.ScoringStrategy;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.PositionCollectingScoreContext;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.SnapshotCorruptedException;

public final class KingAndRobberBaronCapability extends Capability<Void> {

    protected int completedCities, biggestCitySize;
    protected int completedRoads, longestRoadLength;

    private Player king, robberBaron;

    @Override
    public void begin() {
        if (game.hasExpansion(Expansion.COUNT)) {
            //City of Carcassonne is counted as city
            completedCities = 1;
        }
    }

    @Override
    public void handleEvent(Event event) {
       if (event instanceof FeatureCompletedEvent) {
           completed((FeatureCompletedEvent) event);
       }

    }

    private void completed(FeatureCompletedEvent ev) {
        Completable feature = ev.getFeature();
        CompletableScoreContext ctx = ev.getScoreContent();
        if (feature instanceof City) {
            cityCompleted((City) feature, (PositionCollectingScoreContext) ctx);
        }
        if (feature instanceof Road) {
            roadCompleted((Road) feature, (PositionCollectingScoreContext) ctx);
        }
    }

    private void cityCompleted(City c, PositionCollectingScoreContext ctx) {
        completedCities++;
        int size = ctx.getSize();
        if (size > biggestCitySize) {
            biggestCitySize = size;
            king = game.getActivePlayer();
        }
    }

    private void roadCompleted(Road r, PositionCollectingScoreContext ctx) {
        completedRoads++;
        int size = ctx.getSize();
        if (size > longestRoadLength) {
            longestRoadLength = size;
            robberBaron = game.getActivePlayer();
        }
    }

    @Override
    public void finalScoring() {
        if (king != null) {
            king.addPoints(completedCities, PointCategory.BIGGEST_CITY);
        }
        if (robberBaron != null) {
            robberBaron.addPoints(completedRoads, PointCategory.LONGEST_ROAD);
        }
    }

    public int getCompletedCities() {
        return completedCities;
    }

    public int getBiggestCitySize() {
        return biggestCitySize;
    }

    public int getCompletedRoads() {
        return completedRoads;
    }

    public int getLongestRoadLength() {
        return longestRoadLength;
    }

    public Player getKing() {
        return king;
    }

    public Player getRobberBaron() {
        return robberBaron;
    }
}

