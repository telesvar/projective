package com.example.projective.controller;

import com.example.projective.entity.IssueType;
import com.example.projective.entity.Team;
import com.example.projective.repository.TeamRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
class IssueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeamRepository teamRepository;

    private final String TEAM = "team-issue";

    @BeforeEach
    void ensureTeam() {
        teamRepository.findBySlug(TEAM).orElseGet(
                () -> teamRepository.save(Team.builder().name("Team Issue").slug(TEAM).build()));
    }

    private String wsBase() {
        return "/api/v1/teams/" + TEAM + "/workspaces";
    }

    private Long createWorkspace() throws Exception {
        String slug = "wi-" + UUID.randomUUID().toString().substring(0, 6);
        Map<String, Object> ws = new HashMap<>();
        ws.put("name", "WS" + slug);
        ws.put("slug", slug);
        String json = mockMvc.perform(post(wsBase())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ws)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    @Test
    void statusTransition_rules() throws Exception {
        Long wsId = createWorkspace();
        Map<String, Object> dto = new HashMap<>();
        dto.put("title", "Issue1");
        dto.put("type", IssueType.STORY);

        // create
        String issueJson = mockMvc.perform(post(wsBase() + "/" + wsId + "/issues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("TODO")))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(issueJson);
        Long issueId = node.get("id").asLong();

        // illegal skip -> expect 409
        mockMvc.perform(patch(wsBase() + "/" + wsId + "/issues/" + issueId + "/status?status=IN_REVIEW"))
                .andExpect(status().isConflict());

        // legal move
        mockMvc.perform(patch(
                wsBase() + "/" + wsId + "/issues/" + issueId + "/status?status=IN_PROGRESS"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(wsBase() + "/" + wsId + "/issues/" + issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }
}
