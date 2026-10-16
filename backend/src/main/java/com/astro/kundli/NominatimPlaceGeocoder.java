package com.astro.kundli;

import com.astro.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class NominatimPlaceGeocoder implements PlaceGeocoder {

    private final JsonMapper jsonMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Object gate = new Object();
    private long lastRequestAt;

    public NominatimPlaceGeocoder(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public GeoPoint geocode(String place) {
        awaitSlot();
        String query = URLEncoder.encode(place, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&q=" + query))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "AstroGuide/0.1 (local educational astrology app)")
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429 || response.statusCode() >= 500) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Place lookup is unavailable. Try again in a moment.");
            }
            if (response.statusCode() != 200) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Could not find that birthplace. Add the city and country.");
            }
            JsonNode results = jsonMapper.readTree(response.body());
            if (!results.isArray() || results.isEmpty()) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Could not find that birthplace. Add the city and country.");
            }
            JsonNode match = results.get(0);
            return new GeoPoint(match.path("lat").asDouble(), match.path("lon").asDouble(), match.path("display_name").asString());
        } catch (ApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Place lookup is unavailable. Try again in a moment.");
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Place lookup is unavailable. Try again in a moment.");
        }
    }

    private void awaitSlot() {
        synchronized (gate) {
            long wait = 1100 - (System.currentTimeMillis() - lastRequestAt);
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Place lookup is unavailable. Try again in a moment.");
                }
            }
            lastRequestAt = System.currentTimeMillis();
        }
    }
}
