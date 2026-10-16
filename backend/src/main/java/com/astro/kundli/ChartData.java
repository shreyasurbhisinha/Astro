package com.astro.kundli;

import java.time.Instant;
import java.util.List;

public record ChartData(
        Instant utcBirth,
        double ayanamsaDegrees,
        Placement ascendant,
        List<Placement> planets,
        List<HouseCusp> houses,
        List<DashaPeriod> mahadashas
) {
}
