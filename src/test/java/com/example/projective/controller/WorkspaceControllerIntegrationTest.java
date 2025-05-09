package com.example.projective.controller;

import com.example.projective.entity.Team;
import com.example.projective.repository.TeamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "SERVICE_ADMIN")
class WorkspaceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeamRepository teamRepository;

    private final String TEAM = "team-test";

    @BeforeEach
    void ensureTeam() {
        if (teamRepository.findBySlug(TEAM).isEmpty()) {
            teamRepository.save(Team.builder().name("Team Test").slug(TEAM).build());
        }
    }

    private String baseUrl(String suffix) {
        return "/api/v1/teams/" + TEAM + "/workspaces" + suffix;
    }

    @Test
    void createAndFetchWorkspace_flow() throws Exception {
        String slug = "ws-" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> ws = new HashMap<>();
        ws.put("name", "WS" + slug);
        ws.put("slug", slug);

        String createJson = objectMapper.writeValueAsString(ws);

        String response = mockMvc.perform(post(baseUrl(""))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get(baseUrl("/" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug", is(slug)));

        mockMvc.perform(get(baseUrl("")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }
}
