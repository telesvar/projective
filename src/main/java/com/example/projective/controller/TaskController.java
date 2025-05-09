package com.example.projective.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.projective.payload.TaskPayload;
import com.example.projective.service.TaskService;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamSlug}/projects/{projectId}/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','SERVICE_ADMIN')")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    public ResponseEntity<TaskPayload.View> createTask(@PathVariable String teamSlug, @PathVariable Long projectId,
            @Valid @RequestBody TaskPayload.Create requestDTO) {
        TaskPayload.View responseDTO = taskService.createTask(teamSlug, projectId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    public ResponseEntity<List<TaskPayload.View>> getTasks(@PathVariable String teamSlug,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(teamSlug, projectId));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    public ResponseEntity<TaskPayload.View> getTask(@PathVariable String teamSlug, @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(teamSlug, projectId, taskId));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    public ResponseEntity<TaskPayload.View> updateTask(@PathVariable String teamSlug, @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskPayload.Create requestDTO) {
        return ResponseEntity.ok(taskService.updateTask(teamSlug, projectId, taskId, requestDTO));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String teamSlug, @PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.deleteTask(teamSlug, projectId, taskId);
    }
}
