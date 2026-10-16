package com.astro.kundli;

public record Placement(
        String name,
        double longitude,
        String sign,
        double degreeInSign,
        String nakshatra,
        int pada,
        int house,
        boolean retrograde,
        String navamsaSign
) {
}
