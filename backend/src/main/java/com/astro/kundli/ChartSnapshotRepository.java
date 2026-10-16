package com.astro.kundli;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChartSnapshotRepository extends JpaRepository<ChartSnapshot, UUID> {

    Optional<ChartSnapshot> findByProfile_Id(UUID profileId);

    void deleteByProfile_Id(UUID profileId);
}
