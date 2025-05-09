package com.example.projective.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.projective.payload.ProjectPayload;
import com.example.projective.service.ProjectService;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamSlug}/projects")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','SERVICE_ADMIN')")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).ADMIN)")
    public ResponseEntity<ProjectPayload.View> createProject(@PathVariable String teamSlug,
            @Valid @RequestBody ProjectPayload.Create requestDTO) {
        ProjectPayload.View responseDTO = projectService.createProject(teamSlug, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    public ResponseEntity<List<ProjectPayload.View>> getAllProjects(@PathVariable String teamSlug) {
        return ResponseEntity.ok(projectService.getAllProjects(teamSlug));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    public ResponseEntity<ProjectPayload.View> getProject(@PathVariable String teamSlug, @PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(teamSlug, id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).ADMIN)")
    public ResponseEntity<ProjectPayload.View> updateProject(@PathVariable String teamSlug, @PathVariable Long id,
            @Valid @RequestBody ProjectPayload.Create requestDTO) {
        return ResponseEntity.ok(projectService.updateProject(teamSlug, id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).ADMIN)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable String teamSlug, @PathVariable Long id) {
        projectService.deleteProject(teamSlug, id);
    }
}
