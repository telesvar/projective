package com.example.projective.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.projective.entity.Project;
import com.example.projective.entity.ProjectStatus;
import com.example.projective.exception.ResourceNotFoundException;
import com.example.projective.payload.ProjectPayload;
import com.example.projective.repository.ProjectRepository;
import com.example.projective.repository.WorkspaceRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    private ProjectService projectService;

    private static final String TEAM = "unit";
    private static final String WS = "ws";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        var workspace = com.example.projective.entity.Workspace.builder().id(10L).name("WS").slug(WS)
                .team(com.example.projective.entity.Team.builder().id(1L).name("Unit Team").slug(TEAM).build()).build();
        when(workspaceRepository.findBySlugAndTeamSlug(WS, TEAM)).thenReturn(java.util.Optional.of(workspace));
        projectService = new ProjectService(projectRepository, workspaceRepository);
    }

    @Test
    void createProject_shouldReturnSavedDTO() {
        var request = new ProjectPayload.Create("Project", null, null, null, ProjectStatus.NEW);

        Project saved = Project.builder().id(1L).name("Project").status(ProjectStatus.NEW).build();

        when(projectRepository.save(any(Project.class))).thenReturn(saved);

        ProjectPayload.View response = projectService.createProject(TEAM, WS, request);

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.name()).isEqualTo(saved.getName());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void getProjectById_whenExists_shouldReturnDTO() {
        Project project = Project.builder().id(1L).name("Proj").status(ProjectStatus.NEW).build();
        when(projectRepository.findByIdAndWorkspaceSlugAndWorkspaceTeamSlug(1L, WS, TEAM)).thenReturn(Optional.of(project));

        ProjectPayload.View dto = projectService.getProjectById(TEAM, WS, 1L);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Proj");
    }

    @Test
    void getProjectById_whenNotFound_shouldThrow() {
        when(projectRepository.findByIdAndWorkspaceSlugAndWorkspaceTeamSlug(1L, WS, TEAM)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.getProjectById(TEAM, WS, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProject_shouldCallRepositoryDelete() {
        Project project = Project.builder().id(2L).name("Del").build();
        when(projectRepository.findByIdAndWorkspaceSlugAndWorkspaceTeamSlug(2L, WS, TEAM)).thenReturn(Optional.of(project));

        projectService.deleteProject(TEAM, WS, 2L);

        verify(projectRepository).delete(project);
    }

    @Test
    void getAllProjects_shouldReturnMappedList() {
        List<Project> projects = List.of(
                Project.builder().id(1L).name("A").build(),
                Project.builder().id(2L).name("B").build());
        when(projectRepository.findByWorkspaceSlugAndWorkspaceTeamSlug(WS, TEAM)).thenReturn(projects);

        List<ProjectPayload.View> list = projectService.getAllProjects(TEAM, WS);
        assertThat(list).hasSize(2);
        assertThat(list).extracting("name").containsExactlyInAnyOrder("A", "B");
    }
}
