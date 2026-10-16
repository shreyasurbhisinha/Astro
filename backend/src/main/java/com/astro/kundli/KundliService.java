package com.astro.kundli;

import com.astro.common.ApiException;
import com.astro.profile.BirthProfile;
import com.astro.profile.BirthProfileRepository;
import com.astro.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class KundliService {

    private final BirthProfileRepository birthProfileRepository;
    private final ChartSnapshotRepository chartSnapshotRepository;
    private final BirthPlaceResolver birthPlaceResolver;
    private final SwissChartCalculator chartCalculator;
    private final JsonMapper jsonMapper;

    public KundliService(BirthProfileRepository birthProfileRepository,
                         ChartSnapshotRepository chartSnapshotRepository,
                         BirthPlaceResolver birthPlaceResolver,
                         SwissChartCalculator chartCalculator,
                         JsonMapper jsonMapper) {
        this.birthProfileRepository = birthProfileRepository;
        this.chartSnapshotRepository = chartSnapshotRepository;
        this.birthPlaceResolver = birthPlaceResolver;
        this.chartCalculator = chartCalculator;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public KundliResponse get(User user, UUID profileId) {
        BirthProfile profile = birthProfileRepository.findByIdAndUser_Id(profileId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Birth profile not found"));
        return chartSnapshotRepository.findByProfile_Id(profile.getId())
                .map(this::toResponse)
                .orElseGet(() -> calculate(profile));
    }

    private KundliResponse calculate(BirthProfile profile) {
        birthPlaceResolver.ensureLocation(profile);
        ChartData chart = chartCalculator.calculate(
                profile.getDateOfBirth(),
                profile.getTimeOfBirth(),
                ZoneId.of(profile.getTimezone()),
                profile.getLatitude().doubleValue(),
                profile.getLongitude().doubleValue()
        );
        CalculationSettings settings = CalculationSettings.defaults();
        ChartSnapshot snapshot = chartSnapshotRepository.save(new ChartSnapshot(
                profile,
                jsonMapper.writeValueAsString(settings),
                jsonMapper.writeValueAsString(chart)
        ));
        return toResponse(snapshot, profile, settings, chart);
    }

    private KundliResponse toResponse(ChartSnapshot snapshot) {
        CalculationSettings settings = jsonMapper.readValue(snapshot.getCalculationSettings(), CalculationSettings.class);
        ChartData chart = jsonMapper.readValue(snapshot.getChartData(), ChartData.class);
        return toResponse(snapshot, snapshot.getProfile(), settings, chart);
    }

    private KundliResponse toResponse(ChartSnapshot snapshot, BirthProfile profile, CalculationSettings settings, ChartData chart) {
        return new KundliResponse(
                profile.getId(),
                profile.getBirthplace(),
                profile.getLatitude(),
                profile.getLongitude(),
                profile.getTimezone(),
                settings,
                snapshot.getCalculatedAt(),
                chart.utcBirth(),
                chart.ayanamsaDegrees(),
                chart.ascendant(),
                chart.planets(),
                chart.houses(),
                chart.mahadashas(),
                VedicMath.currentMahadasha(chart.mahadashas(), Instant.now())
        );
    }
}
