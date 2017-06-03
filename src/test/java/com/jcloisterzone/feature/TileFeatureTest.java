package com.jcloisterzone.feature;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;

public class TileFeatureTest {

    @Test
    public void placeOnBoard() {
        Game game = new Game("12345678");
        Road r = new Road(game,
          List.of(new FeaturePointer(Position.ZERO, Location.N)),
          List.of(new Edge(Position.ZERO, new Position(0, -1)))
        );

        Road placed = r.placeOnBoard(new Position(1, 0), Rotation.R0);
        assertEquals(
            new FeaturePointer(new Position(1, 0), Location.N),
            placed.getPlaces().get()
        );
        assertEquals(
            new Edge(new Position(1, 0), Location.N),
            placed.getOpenEdges().get()
        );

        placed = r.placeOnBoard(new Position(1, 2), Rotation.R90);
        assertEquals(
            new FeaturePointer(new Position(1, 2), Location.E),
            placed.getPlaces().get()
        );
        assertEquals(
            new Edge(new Position(1, 2), Location.E),
            placed.getOpenEdges().get()
        );
    }

}
