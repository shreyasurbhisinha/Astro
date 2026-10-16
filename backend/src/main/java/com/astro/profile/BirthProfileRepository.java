package com.astro.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BirthProfileRepository extends JpaRepository<BirthProfile, UUID> {

    List<BirthProfile> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<BirthProfile> findByIdAndUser_Id(UUID id, UUID userId);
}
