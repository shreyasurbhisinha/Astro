package com.astro.kundli;

import com.astro.profile.BirthProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "chart_snapshots")
public class ChartSnapshot {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private BirthProfile profile;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_settings", nullable = false)
    private String calculationSettings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chart_data", nullable = false)
    private String chartData;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @Column(name = "calculated_at", nullable = false, updatable = false)
    private Instant calculatedAt;

    protected ChartSnapshot() {
    }

    public ChartSnapshot(BirthProfile profile, String calculationSettings, String chartData) {
        this.profile = profile;
        this.calculationSettings = calculationSettings;
        this.chartData = chartData;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (calculatedAt == null) {
            calculatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        }
    }

    public BirthProfile getProfile() {
        return profile;
    }

    public String getCalculationSettings() {
        return calculationSettings;
    }

    public String getChartData() {
        return chartData;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
