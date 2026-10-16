package com.astro.kundli;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KundliIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void savesAChartAndKeepsItPrivate() throws Exception {
        String token = register("kundli-" + UUID.randomUUID() + "@example.com");
        MvcResult created = mockMvc.perform(post("/api/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Independence",
                                  "dateOfBirth": "1947-08-15",
                                  "timeOfBirth": "00:00",
                                  "birthplace": "New Delhi, India",
                                  "latitude": 28.6139,
                                  "longitude": 77.2090,
                                  "timezone": "Asia/Kolkata"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String profileId = read(created.getResponse().getContentAsString(), "$.id");

        MvcResult first = mockMvc.perform(get("/api/profiles/" + profileId + "/kundli")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ascendant.sign").value("Taurus"))
                .andExpect(jsonPath("$.planets[1].nakshatra").value("Pushya"))
                .andExpect(jsonPath("$.settings.ayanamsa").value("lahiri"))
                .andReturn();
        String calculatedAt = read(first.getResponse().getContentAsString(), "$.calculatedAt");

        mockMvc.perform(get("/api/profiles/" + profileId + "/kundli")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatedAt").value(calculatedAt));

        String other = register("other-kundli-" + UUID.randomUUID() + "@example.com");
        mockMvc.perform(get("/api/profiles/" + profileId + "/kundli")
                        .header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
    }

    private String register(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result.getResponse().getContentAsString(), "$.token");
    }
}
