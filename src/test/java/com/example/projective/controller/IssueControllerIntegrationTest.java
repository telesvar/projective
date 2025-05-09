package com.example.projective.controller;

import com.example.projective.entity.IssueType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "SERVICE_ADMIN")
@Transactional
class IssueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private com.example.projective.repository.TeamRepository teamRepo;

    @Autowired
    private com.example.projective.repository.WorkspaceRepository wsRepo;

    private static final String TEAM = "int-team";
    private static final String WS = "int-ws";

    @BeforeEach
    void ensureWorkspace() {
        var team = teamRepo.findBySlug(TEAM)
                .orElseGet(() -> teamRepo.save(com.example.projective.entity.Team.builder().name("Team I").slug(TEAM).build()));
        wsRepo.findBySlugAndTeamSlug(WS, TEAM)
                .orElseGet(() -> wsRepo.save(com.example.projective.entity.Workspace.builder().name("WS I").slug(WS).team(team).build()));
    }

    private String projectBase() {
        return "/api/v1/teams/" + TEAM + "/workspaces/" + WS + "/projects";
    }

    private Long createProject(String name) throws Exception {
        Map<String, Object> body = Map.of("name", name);
        String json = mockMvc.perform(post(projectBase())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(json);
        return node.get("id").asLong();
    }

    private String issueBase(Long projectId) {
        return projectBase() + "/" + projectId + "/issues";
    }

    @Test
    void lifecycle_create_update_status() throws Exception {
        Long projectId = createProject("IssueProject");
        Map<String, Object> dto = new HashMap<>();
        dto.put("title", "My Issue");
        dto.put("type", IssueType.TASK);

        // create
        String resp = mockMvc.perform(post(issueBase(projectId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("TODO")))
                .andReturn().getResponse().getContentAsString();
        Long issueId = mapper.readTree(resp).get("id").asLong();

        // update title
        dto.put("title", "New Title");
        mockMvc.perform(put(issueBase(projectId) + "/" + issueId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New Title")));

        // change status legally TODO -> IN_PROGRESS
        mockMvc.perform(patch(issueBase(projectId) + "/" + issueId + "/status?status=IN_PROGRESS"))
                .andExpect(status().isNoContent());

        // fetch verify
        mockMvc.perform(get(issueBase(projectId) + "/" + issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }
}
