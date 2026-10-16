package com.astro.kundli;

import com.astro.common.ApiException;
import com.astro.profile.BirthProfile;
import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;

@Service
public class BirthPlaceResolver {

    private final PlaceGeocoder placeGeocoder;
    private final ObjectProvider<TimeZoneEngine> timeZones;

    public BirthPlaceResolver(PlaceGeocoder placeGeocoder, ObjectProvider<TimeZoneEngine> timeZones) {
        this.placeGeocoder = placeGeocoder;
        this.timeZones = timeZones;
    }

    public void ensureLocation(BirthProfile profile) {
        if (profile.getLatitude() == null || profile.getLongitude() == null) {
            GeoPoint point = placeGeocoder.geocode(profile.getBirthplace());
            profile.setLocation(coordinate(point.latitude()), coordinate(point.longitude()), profile.getTimezone());
        }
        if (profile.getTimezone() == null || profile.getTimezone().isBlank()) {
            ZoneId zone = timeZones.getObject()
                    .query(profile.getLatitude().doubleValue(), profile.getLongitude().doubleValue())
                    .orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Could not determine the time zone for that location."));
            profile.setLocation(profile.getLatitude(), profile.getLongitude(), zone.getId());
        }
    }

    private static BigDecimal coordinate(double value) {
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }
}
