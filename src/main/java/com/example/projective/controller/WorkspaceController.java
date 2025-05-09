package com.example.projective.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.projective.payload.WorkspacePayload;
import com.example.projective.service.WorkspaceService;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamSlug}/workspaces")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','SERVICE_ADMIN')")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).ADMIN)")
    @PostMapping
    public ResponseEntity<WorkspacePayload.View> create(@PathVariable String teamSlug,
            @Valid @RequestBody WorkspacePayload.Create dto) {
        var response = workspaceService.createWorkspace(teamSlug, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    @GetMapping
    public ResponseEntity<List<WorkspacePayload.View>> list(@PathVariable String teamSlug) {
        return ResponseEntity.ok(workspaceService.getAll(teamSlug));
    }

    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    @GetMapping("/{id}")
    public ResponseEntity<WorkspacePayload.View> get(@PathVariable String teamSlug, @PathVariable Long id) {
        return ResponseEntity.ok(workspaceService.getById(teamSlug, id));
    }
}
