package com.astro.kundli;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VedicMathTest {

    @Test
    void mapsSignsNakshatrasAndNavamsa() {
        assertEquals("Aries", VedicMath.sign(0));
        assertEquals("Aries", VedicMath.navamsaSign(0));
        assertEquals("Cancer", VedicMath.navamsaSign(10));
        assertEquals("Ashwini", VedicMath.nakshatra(0));
        assertEquals(1, VedicMath.pada(0));
        assertEquals("Pushya", VedicMath.nakshatra(93.98));
        assertEquals(1, VedicMath.pada(93.98));
        assertEquals(3, VedicMath.wholeSignHouse(VedicMath.signIndex(117.99), VedicMath.signIndex(37.73)));
    }

    @Test
    void startsVimshottariFromTheMoonNakshatra() {
        var periods = VedicMath.mahadashas(93.98, Instant.parse("1947-08-14T18:30:00Z"));
        assertEquals("Saturn", periods.getFirst().lord());
        assertTrue(periods.getFirst().end().isBefore(Instant.parse("1966-01-01T00:00:00Z")));
        assertEquals(9, periods.size());
    }
}
