package com.example.projective;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    String username = "testuser";
    String password = "password";

    @BeforeEach
    void setup() throws Exception {
        java.util.Map<String, Object> req = java.util.Map.of(
                "username", username,
                "password", password);
        mockMvc.perform(post("/api/v1/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void signInAndAccessProtectedEndpoint() throws Exception {
        java.util.Map<String, Object> credentials = java.util.Map.of(
                "username", username,
                "password", password);

        String token = mockMvc.perform(post("/api/v1/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        // Extract token string without JSON parsing util quick and dirty
        token = mapper.readTree(token).get("token").asText();

        // Without token should be ok (public endpoint)
        mockMvc.perform(get("/api/v1/health/ping"))
                .andExpect(status().isOk());

        // With token should also be ok (200)
        mockMvc.perform(get("/api/v1/health/ping")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
