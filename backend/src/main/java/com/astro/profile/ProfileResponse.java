package com.astro.profile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String name,
        LocalDate dateOfBirth,
        LocalTime timeOfBirth,
        String birthplace,
        BigDecimal latitude,
        BigDecimal longitude,
        String timezone,
        Instant createdAt
) {
    static ProfileResponse from(BirthProfile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getDateOfBirth(),
                profile.getTimeOfBirth(),
                profile.getBirthplace(),
                profile.getLatitude(),
                profile.getLongitude(),
                profile.getTimezone(),
                profile.getCreatedAt()
        );
    }
}
