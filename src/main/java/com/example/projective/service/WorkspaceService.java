package com.example.projective.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projective.entity.Team;
import com.example.projective.exception.ResourceNotFoundException;
import com.example.projective.payload.WorkspacePayload;
import com.example.projective.repository.TeamRepository;
import com.example.projective.repository.WorkspaceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final TeamRepository teamRepository;

    public WorkspacePayload.View createWorkspace(String teamSlug, WorkspacePayload.Create dto) {
        if (workspaceRepository.existsBySlugAndTeamSlug(dto.slug(), teamSlug)) {
            throw new IllegalArgumentException("Workspace slug already exists within team");
        }
        Team team = teamRepository.findBySlug(teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamSlug));
        var entity = new com.example.projective.entity.Workspace();
        entity.setName(dto.name());
        entity.setSlug(dto.slug());
        entity.setTeam(team);
        var saved = workspaceRepository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkspacePayload.View> getAll(String teamSlug) {
        Team team = teamRepository.findBySlug(teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamSlug));
        return team.getWorkspaces().stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkspacePayload.View getById(String teamSlug, Long id) {
        var ws = workspaceRepository.findById(id)
                .filter(w -> w.getTeam().getSlug().equals(teamSlug))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workspace not found with id " + id + " in team " + teamSlug));
        return toView(ws);
    }

    private WorkspacePayload.View toView(com.example.projective.entity.Workspace ws) {
        return new WorkspacePayload.View(ws.getId(), ws.getName(), ws.getSlug());
    }
}
