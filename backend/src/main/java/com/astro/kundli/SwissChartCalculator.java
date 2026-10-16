package com.astro.kundli;

import com.astro.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class SwissChartCalculator {

    private static final int FLAGS = SweConst.SEFLG_MOSEPH | SweConst.SEFLG_SPEED | SweConst.SEFLG_SIDEREAL;

    private final SwissEph ephemeris = new SwissEph();
    private final Object lock = new Object();

    public ChartData calculate(LocalDate date, LocalTime time, ZoneId zone, double latitude, double longitude) {
        ZonedDateTime utc = LocalDateTime.of(date, time).atZone(zone).withZoneSameInstant(ZoneOffset.UTC);
        double hour = utc.getHour()
                + utc.getMinute() / 60.0
                + utc.getSecond() / 3600.0
                + utc.getNano() / 3_600_000_000_000.0;
        double julianDay = SweDate.getJulDay(utc.getYear(), utc.getMonthValue(), utc.getDayOfMonth(), hour);

        synchronized (lock) {
            ephemeris.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);
            double ascendant = ascendant(julianDay, latitude, longitude);
            int ascendantSign = VedicMath.signIndex(ascendant);
            List<Placement> planets = planets(julianDay, ascendantSign);
            double moon = planets.stream()
                    .filter(planet -> planet.name().equals("Moon"))
                    .mapToDouble(Placement::longitude)
                    .findFirst()
                    .orElseThrow();
            return new ChartData(
                    utc.toInstant(),
                    round4(ephemeris.swe_get_ayanamsa_ut(julianDay)),
                    VedicMath.placement("Ascendant", ascendant, 1, ascendantSign),
                    planets,
                    houses(ascendantSign),
                    VedicMath.mahadashas(moon, utc.toInstant())
            );
        }
    }

    private double ascendant(double julianDay, double latitude, double longitude) {
        double[] cusps = new double[40];
        double[] points = new double[20];
        int result = ephemeris.swe_houses(julianDay, FLAGS, latitude, longitude, 'W', cusps, points);
        if (result < 0 || Double.isNaN(points[0])) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Chart calculation failed");
        }
        return points[0];
    }

    private List<Placement> planets(double julianDay, int ascendantSign) {
        int[] bodies = {
                SweConst.SE_SUN, SweConst.SE_MOON, SweConst.SE_MARS, SweConst.SE_MERCURY,
                SweConst.SE_JUPITER, SweConst.SE_VENUS, SweConst.SE_SATURN, SweConst.SE_MEAN_NODE
        };
        String[] names = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu"};
        List<Placement> planets = new ArrayList<>();
        double[] coordinates = new double[6];
        StringBuffer error = new StringBuffer();
        for (int index = 0; index < bodies.length; index++) {
            int status = ephemeris.swe_calc_ut(julianDay, bodies[index], FLAGS, coordinates, error);
            if (status < 0) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Chart calculation failed");
            }
            planets.add(VedicMath.placement(names[index], coordinates[0], coordinates[3], ascendantSign));
            if (bodies[index] == SweConst.SE_MEAN_NODE) {
                planets.add(VedicMath.placement("Ketu", coordinates[0] + 180.0, coordinates[3], ascendantSign));
            }
        }
        return List.copyOf(planets);
    }

    private static List<HouseCusp> houses(int ascendantSign) {
        List<HouseCusp> houses = new ArrayList<>();
        for (int house = 0; house < 12; house++) {
            houses.add(new HouseCusp(house + 1, VedicMath.SIGNS[(ascendantSign + house) % 12]));
        }
        return List.copyOf(houses);
    }

    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
