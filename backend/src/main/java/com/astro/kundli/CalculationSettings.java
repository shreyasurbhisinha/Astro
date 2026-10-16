package com.astro.kundli;

public record CalculationSettings(
        String zodiac,
        String ayanamsa,
        String houseSystem,
        String node,
        String ephemeris
) {
    public static CalculationSettings defaults() {
        return new CalculationSettings("sidereal", "lahiri", "whole_sign", "mean", "moshier");
    }
}
