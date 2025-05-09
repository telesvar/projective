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
@RequestMapping("/api/v1/teams/{teamSlug}/workspaces/{workspaceSlug}/projects/{projectId}/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','SERVICE_ADMIN')")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    public ResponseEntity<TaskPayload.View> createTask(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId,
            @Valid @RequestBody TaskPayload.Create requestDTO) {
        TaskPayload.View responseDTO = taskService.createTask(teamSlug, workspaceSlug, projectId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).VIEWER)")
    public ResponseEntity<List<TaskPayload.View>> getTasks(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(teamSlug, workspaceSlug, projectId));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).VIEWER)")
    public ResponseEntity<TaskPayload.View> getTask(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(teamSlug, workspaceSlug, projectId, taskId));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    public ResponseEntity<TaskPayload.View> updateTask(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskPayload.Create requestDTO) {
        return ResponseEntity.ok(taskService.updateTask(teamSlug, workspaceSlug, projectId, taskId, requestDTO));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        taskService.deleteTask(teamSlug, workspaceSlug, projectId, taskId);
    }
}
