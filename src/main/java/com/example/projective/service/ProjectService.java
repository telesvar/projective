package com.example.projective.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projective.entity.Project;
// import com.example.projective.entity.Team; // removed unused
import com.example.projective.exception.ResourceNotFoundException;
import com.example.projective.payload.ProjectPayload;
import com.example.projective.repository.ProjectRepository;
import com.example.projective.repository.WorkspaceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    public ProjectPayload.View createProject(String teamSlug, String workspaceSlug, ProjectPayload.Create dto) {
        var workspace = workspaceRepository.findBySlugAndTeamSlug(workspaceSlug, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceSlug));
        Project project = new Project();
        project.setName(dto.name());
        project.setDescription(dto.description());
        project.setStartDate(dto.startDate());
        project.setEndDate(dto.endDate());
        project.setStatus(dto.status());
        project.setWorkspace(workspace);
        workspace.addProject(project);
        Project saved = projectRepository.save(project);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectPayload.View> getAllProjects(String teamSlug, String workspaceSlug) {
        return projectRepository.findByWorkspaceSlugAndWorkspaceTeamSlug(workspaceSlug, teamSlug).stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectPayload.View getProjectById(String teamSlug, String workspaceSlug, Long id) {
        Project project = projectRepository.findByIdAndWorkspaceSlugAndWorkspaceTeamSlug(id, workspaceSlug, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + id + " in workspace " + workspaceSlug + ", team " + teamSlug));
        return toView(project);
    }

    public ProjectPayload.View updateProject(String teamSlug, String workspaceSlug, Long id, ProjectPayload.Create dto) {
        Project project = projectRepository.findByIdAndWorkspaceSlugAndWorkspaceTeamSlug(id, workspaceSlug, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + id + " in workspace " + workspaceSlug));
        if (dto.name() != null)
            project.setName(dto.name());
        project.setDescription(dto.description());
        project.setStartDate(dto.startDate());
        project.setEndDate(dto.endDate());
        project.setStatus(dto.status());
        Project updated = projectRepository.save(project);
        return toView(updated);
    }

    public void deleteProject(String teamSlug, String workspaceSlug, Long id) {
        Project project = projectRepository.findByIdAndWorkspaceSlugAndWorkspaceTeamSlug(id, workspaceSlug, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + id + " in workspace " + workspaceSlug));
        projectRepository.delete(project);
    }

    private ProjectPayload.View toView(Project p) {
        return new ProjectPayload.View(p.getId(), p.getName(), p.getDescription(), p.getStartDate(), p.getEndDate(),
                p.getStatus());
    }
}
