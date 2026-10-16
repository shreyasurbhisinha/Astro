package com.astro.profile;

import com.astro.common.ApiException;
import com.astro.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {

    private final BirthProfileRepository birthProfileRepository;

    public ProfileService(BirthProfileRepository birthProfileRepository) {
        this.birthProfileRepository = birthProfileRepository;
    }

    @Transactional
    public ProfileResponse create(User user, ProfileRequest request) {
        BirthProfile profile = new BirthProfile(
                user,
                request.name().trim(),
                request.dateOfBirth(),
                request.timeOfBirth(),
                request.birthplace().trim()
        );
        profile.update(
                request.name().trim(),
                request.dateOfBirth(),
                request.timeOfBirth(),
                request.birthplace().trim(),
                request.latitude(),
                request.longitude(),
                blankToNull(request.timezone())
        );
        return ProfileResponse.from(birthProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> list(User user) {
        return birthProfileRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(ProfileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(User user, UUID profileId) {
        return ProfileResponse.from(findOwned(user, profileId));
    }

    @Transactional
    public ProfileResponse update(User user, UUID profileId, ProfileRequest request) {
        BirthProfile profile = findOwned(user, profileId);
        profile.update(
                request.name().trim(),
                request.dateOfBirth(),
                request.timeOfBirth(),
                request.birthplace().trim(),
                request.latitude(),
                request.longitude(),
                blankToNull(request.timezone())
        );
        return ProfileResponse.from(profile);
    }

    @Transactional
    public void delete(User user, UUID profileId) {
        birthProfileRepository.delete(findOwned(user, profileId));
    }

    private BirthProfile findOwned(User user, UUID profileId) {
        return birthProfileRepository.findByIdAndUser_Id(profileId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Birth profile not found"));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
