package com.astro.kundli;

import com.astro.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles/{profileId}/kundli")
public class KundliController {

    private final KundliService kundliService;

    public KundliController(KundliService kundliService) {
        this.kundliService = kundliService;
    }

    @GetMapping
    KundliResponse get(Authentication authentication, @PathVariable UUID profileId) {
        return kundliService.get((User) authentication.getPrincipal(), profileId);
    }
}
