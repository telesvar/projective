package com.example.projective.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.projective.payload.TeamPayload;
import com.example.projective.service.TeamService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamPayload.View> create(@Valid @RequestBody TeamPayload.Create dto, Authentication auth) {
        TeamPayload.View response = teamService.create(dto, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<TeamPayload.View> myTeams(Authentication auth) {
        return teamService.listMine(auth.getName());
    }

    @GetMapping("/{slug}")
    @PreAuthorize("@authz.hasTeamRoleAtLeast(#slug, authentication, T(com.example.projective.entity.TeamRole).VIEWER)")
    public TeamPayload.View one(@PathVariable String slug) {
        return teamService.getBySlug(slug);
    }
}
