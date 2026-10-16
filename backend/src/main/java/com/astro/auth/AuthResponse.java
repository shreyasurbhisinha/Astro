package com.astro.auth;

public record AuthResponse(String token, UserResponse user) {
}
