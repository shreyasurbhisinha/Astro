package com.astro.kundli;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LahiriChartTest {

    @Test
    void independenceChartMatchesTraditionalPlacements() {
        ChartData chart = new SwissChartCalculator().calculate(
                LocalDate.of(1947, 8, 15),
                LocalTime.MIDNIGHT,
                ZoneId.of("Asia/Kolkata"),
                28.6139,
                77.2090
        );

        assertEquals("Taurus", chart.ascendant().sign());
        assertEquals("Cancer", planet(chart, "Sun").sign());
        assertEquals(3, planet(chart, "Sun").house());
        assertEquals("Cancer", planet(chart, "Moon").sign());
        assertEquals("Pushya", planet(chart, "Moon").nakshatra());
        assertEquals(1, planet(chart, "Moon").pada());
        assertEquals("Gemini", planet(chart, "Mars").sign());
        assertEquals("Libra", planet(chart, "Jupiter").sign());
        assertEquals("Taurus", planet(chart, "Rahu").sign());
        assertEquals("Scorpio", planet(chart, "Ketu").sign());
        assertEquals("Saturn", chart.mahadashas().getFirst().lord());
        assertEquals("Taurus", chart.houses().getFirst().sign());
    }

    private static Placement planet(ChartData chart, String name) {
        return chart.planets().stream()
                .filter(placement -> placement.name().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
