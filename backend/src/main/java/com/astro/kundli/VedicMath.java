package com.astro.kundli;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class VedicMath {

    static final String[] SIGNS = {
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    };

    private static final String[] NAKSHATRAS = {
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
            "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
            "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
            "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
            "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    };

    private static final String[] DASHA_LORDS = {
            "Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"
    };

    private static final double[] DASHA_YEARS = {7, 20, 6, 10, 7, 18, 16, 19, 17};

    private static final double YEAR_SECONDS = 365.2425 * 86400.0;
    private static final double NAKSHATRA_SPAN = 360.0 / 27.0;

    private VedicMath() {
    }

    public static double normalize(double longitude) {
        double value = longitude % 360.0;
        if (value < 0) {
            value += 360.0;
        }
        return value;
    }

    public static String sign(double longitude) {
        return SIGNS[signIndex(longitude)];
    }

    public static int signIndex(double longitude) {
        int index = (int) Math.floor(normalize(longitude) / 30.0);
        return Math.min(index, 11);
    }

    public static String nakshatra(double longitude) {
        return NAKSHATRAS[nakshatraIndex(longitude)];
    }

    public static int pada(double longitude) {
        double into = normalize(longitude) - nakshatraIndex(longitude) * NAKSHATRA_SPAN;
        int pada = (int) Math.floor(into / (NAKSHATRA_SPAN / 4.0)) + 1;
        return Math.min(Math.max(pada, 1), 4);
    }

    public static String navamsaSign(double longitude) {
        double lon = normalize(longitude);
        int sign = signIndex(lon);
        int part = (int) Math.floor((lon % 30.0) / (10.0 / 3.0));
        if (part > 8) {
            part = 8;
        }
        int start = switch (sign % 4) {
            case 0 -> 0;
            case 1 -> 9;
            case 2 -> 6;
            default -> 3;
        };
        return SIGNS[(start + part) % 12];
    }

    public static int wholeSignHouse(int planetSignIndex, int ascendantSignIndex) {
        return ((planetSignIndex - ascendantSignIndex + 12) % 12) + 1;
    }

    public static Placement placement(String name, double longitude, double speed, int ascendantSignIndex) {
        double lon = normalize(longitude);
        int signIndex = signIndex(lon);
        return new Placement(
                name,
                round(lon),
                SIGNS[signIndex],
                round(lon % 30.0),
                nakshatra(lon),
                pada(lon),
                wholeSignHouse(signIndex, ascendantSignIndex),
                speed < 0,
                navamsaSign(lon)
        );
    }

    public static List<DashaPeriod> mahadashas(double moonLongitude, Instant birth) {
        double lon = normalize(moonLongitude);
        int nakshatra = nakshatraIndex(lon);
        double into = lon - nakshatra * NAKSHATRA_SPAN;
        double remaining = 1.0 - (into / NAKSHATRA_SPAN);
        int firstLord = nakshatra % 9;
        List<DashaPeriod> periods = new ArrayList<>();
        Instant cursor = birth;
        for (int step = 0; step < 9; step++) {
            int lord = (firstLord + step) % 9;
            double years = DASHA_YEARS[lord] * (step == 0 ? remaining : 1.0);
            Instant end = cursor.plusSeconds(Math.round(years * YEAR_SECONDS));
            periods.add(new DashaPeriod(DASHA_LORDS[lord], cursor, end));
            cursor = end;
        }
        return List.copyOf(periods);
    }

    public static String currentMahadasha(List<DashaPeriod> periods, Instant now) {
        for (DashaPeriod period : periods) {
            if (!now.isBefore(period.start()) && now.isBefore(period.end())) {
                return period.lord();
            }
        }
        return periods.get(periods.size() - 1).lord();
    }

    private static int nakshatraIndex(double longitude) {
        int index = (int) Math.floor(normalize(longitude) / NAKSHATRA_SPAN);
        return Math.min(index, 26);
    }

    private static double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
