package com.astro.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "astro.jwt")
public record JwtProperties(String secret, long expirationMinutes) {
}
