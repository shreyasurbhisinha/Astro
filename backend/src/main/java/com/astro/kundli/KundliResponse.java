package com.astro.kundli;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record KundliResponse(
        UUID profileId,
        String birthplace,
        BigDecimal latitude,
        BigDecimal longitude,
        String timezone,
        CalculationSettings settings,
        Instant calculatedAt,
        Instant utcBirth,
        double ayanamsaDegrees,
        Placement ascendant,
        List<Placement> planets,
        List<HouseCusp> houses,
        List<DashaPeriod> mahadashas,
        String currentMahadasha
) {
}
