package com.example.projective.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "SERVICE_ADMIN")
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.projective.repository.TeamRepository teamRepository;

    private static final String TEAM = "test-int";

    @BeforeEach
    void setupTeam() {
        if (teamRepository.findBySlug(TEAM).isEmpty()) {
            teamRepository
                    .save(com.example.projective.entity.Team.builder().name("Test Team").slug(TEAM).build());
        }
    }

    private String base() {
        return "/api/v1/teams/" + TEAM + "/projects";
    }

    @Test
    void createProject_shouldReturnCreated() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Integration Project");
        request.put("description", "Desc");
        request.put("startDate", LocalDate.now());

        ResultActions result = mockMvc.perform(post(base())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Integration Project")));
    }

    @Test
    void createProject_whenNameMissing_shouldReturnBadRequest() throws Exception {
        Map<String, Object> request = new HashMap<>();
        // name intentionally blank

        mockMvc.perform(post(base())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name", notNullValue()));
    }
}
