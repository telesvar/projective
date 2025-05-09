package com.example.projective.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projective.entity.Project;
import com.example.projective.entity.Team;
import com.example.projective.exception.ResourceNotFoundException;
import com.example.projective.payload.ProjectPayload;
import com.example.projective.repository.ProjectRepository;
import com.example.projective.repository.TeamRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;

    public ProjectPayload.View createProject(String teamSlug, ProjectPayload.Create dto) {
        Team team = teamRepository.findBySlug(teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamSlug));
        Project project = new Project();
        project.setName(dto.name());
        project.setDescription(dto.description());
        project.setStartDate(dto.startDate());
        project.setEndDate(dto.endDate());
        project.setStatus(dto.status());
        project.setTeam(team);
        team.addProject(project);
        Project saved = projectRepository.save(project);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectPayload.View> getAllProjects(String teamSlug) {
        return projectRepository.findByTeamSlug(teamSlug).stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectPayload.View getProjectById(String teamSlug, Long id) {
        Project project = projectRepository.findByIdAndTeamSlug(id, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + id + " in team " + teamSlug));
        return toView(project);
    }

    public ProjectPayload.View updateProject(String teamSlug, Long id, ProjectPayload.Create dto) {
        Project project = projectRepository.findByIdAndTeamSlug(id, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + id + " in team " + teamSlug));
        if (dto.name() != null)
            project.setName(dto.name());
        project.setDescription(dto.description());
        project.setStartDate(dto.startDate());
        project.setEndDate(dto.endDate());
        project.setStatus(dto.status());
        Project updated = projectRepository.save(project);
        return toView(updated);
    }

    public void deleteProject(String teamSlug, Long id) {
        Project project = projectRepository.findByIdAndTeamSlug(id, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + id + " in team " + teamSlug));
        projectRepository.delete(project);
    }

    private ProjectPayload.View toView(Project p) {
        return new ProjectPayload.View(p.getId(), p.getName(), p.getDescription(), p.getStartDate(), p.getEndDate(),
                p.getStatus());
    }
}
