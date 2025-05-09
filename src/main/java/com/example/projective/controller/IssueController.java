package com.example.projective.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.projective.entity.IssueStatus;
import com.example.projective.payload.IssuePayload;
import com.example.projective.service.IssueService;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamSlug}/workspaces/{workspaceSlug}/projects/{projectId}/issues")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','SERVICE_ADMIN')")
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    public ResponseEntity<IssuePayload.View> create(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId,
            @Valid @RequestBody IssuePayload.Create dto) {
        var response = issueService.createIssue(teamSlug, workspaceSlug, projectId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).VIEWER)")
    public ResponseEntity<List<IssuePayload.View>> list(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(issueService.getIssues(teamSlug, workspaceSlug, projectId));
    }

    @GetMapping("/{issueId}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).VIEWER)")
    public ResponseEntity<IssuePayload.View> get(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId,
            @PathVariable Long issueId) {
        return ResponseEntity.ok(issueService.getIssue(teamSlug, workspaceSlug, projectId, issueId));
    }

    @PutMapping("/{issueId}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    public ResponseEntity<IssuePayload.View> update(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @Valid @RequestBody IssuePayload.Create dto) {
        return ResponseEntity.ok(issueService.updateIssue(teamSlug, workspaceSlug, projectId, issueId, dto));
    }

    @PatchMapping("/{issueId}/status")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @RequestParam IssueStatus status) {
        issueService.changeStatus(teamSlug, workspaceSlug, projectId, issueId, status);
    }

    @DeleteMapping("/{issueId}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#teamSlug, authentication, T(com.example.projective.entity.TeamRole).MEMBER)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String teamSlug,
            @PathVariable String workspaceSlug,
            @PathVariable Long projectId,
            @PathVariable Long issueId) {
        issueService.deleteIssue(teamSlug, workspaceSlug, projectId, issueId);
    }
}
