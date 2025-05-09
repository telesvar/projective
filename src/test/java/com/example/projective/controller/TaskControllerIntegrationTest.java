package com.example.projective.controller;

import com.example.projective.entity.TaskPriority;
import com.example.projective.entity.TaskStatus;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "SERVICE_ADMIN")
class TaskControllerIntegrationTest {

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

    private String projectBase() {
        return "/api/v1/teams/" + TEAM + "/projects";
    }

    private String taskBase(Long projectId) {
        return "/api/v1/teams/" + TEAM + "/projects/" + projectId + "/tasks";
    }

    private Long createProject() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("name", "TaskProj");
        String content = mockMvc.perform(post(projectBase())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(content);
        return node.get("id").asLong();
    }

    @Test
    void createAndFetchTask_flow() throws Exception {
        Long projectId = createProject();

        Map<String, Object> taskReq = new HashMap<>();
        taskReq.put("name", "TaskInt");
        taskReq.put("status", TaskStatus.TODO);
        taskReq.put("priority", TaskPriority.HIGH);

        // create task
        String taskJson = mockMvc.perform(post(taskBase(projectId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("TaskInt")))
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(taskJson).get("id").asLong();

        // get task
        mockMvc.perform(get(taskBase(projectId) + "/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(taskId.intValue())))
                .andExpect(jsonPath("$.name", is("TaskInt")));

        // list tasks
        String listJson = mockMvc.perform(get(taskBase(projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        List<?> list = objectMapper.readValue(listJson, new TypeReference<List<?>>() {
        });
        org.assertj.core.api.Assertions.assertThat(list).hasSize(1);
    }
}
