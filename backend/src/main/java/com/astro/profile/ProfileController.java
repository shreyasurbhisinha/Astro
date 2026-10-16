package com.astro.profile;

import com.astro.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    List<ProfileResponse> list(Authentication authentication) {
        return profileService.list(currentUser(authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProfileResponse create(Authentication authentication, @Valid @RequestBody ProfileRequest request) {
        return profileService.create(currentUser(authentication), request);
    }

    @GetMapping("/{id}")
    ProfileResponse get(Authentication authentication, @PathVariable UUID id) {
        return profileService.get(currentUser(authentication), id);
    }

    @PutMapping("/{id}")
    ProfileResponse update(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody ProfileRequest request) {
        return profileService.update(currentUser(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        profileService.delete(currentUser(authentication), id);
        return ResponseEntity.noContent().build();
    }

    private static User currentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
