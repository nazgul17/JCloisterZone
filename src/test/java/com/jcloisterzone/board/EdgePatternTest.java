package com.jcloisterzone.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EdgePatternTest {


    @Test
    public void rotate() {
        assertEquals("RC?F", EdgePattern.fromString("RC?F").rotate(Rotation.R0).toString());
        assertEquals("FRC?", EdgePattern.fromString("RC?F").rotate(Rotation.R90).toString());
        assertEquals("?FRC", EdgePattern.fromString("RC?F").rotate(Rotation.R180).toString());
        assertEquals("C?FR", EdgePattern.fromString("RC?F").rotate(Rotation.R270).toString());
    }

    @Test
    public void equals() {
        assertEquals(EdgePattern.fromString("RC?F"), EdgePattern.fromString("RC?F"));
        assertEquals(EdgePattern.fromString("RC?F"), EdgePattern.fromString("FRC?"));
        assertEquals(EdgePattern.fromString("RC?F"), EdgePattern.fromString("?FRC"));
        assertEquals(EdgePattern.fromString("RC?F"), EdgePattern.fromString("C?FR"));
    }

    @Test
    public void isMatching() {
        assertTrue(EdgePattern.fromString("RC?F").isMatching(EdgePattern.fromString("RCRF")));
        assertTrue(EdgePattern.fromString("RC?F").isMatching(EdgePattern.fromString("FFRC")));
        assertTrue(EdgePattern.fromString("????").isMatching(EdgePattern.fromString("IRIF")));
    }

}
