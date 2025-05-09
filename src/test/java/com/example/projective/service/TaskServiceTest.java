package com.example.projective.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.projective.entity.*;
import com.example.projective.exception.ResourceNotFoundException;
import com.example.projective.payload.TaskPayload;
import com.example.projective.repository.ProjectRepository;
import com.example.projective.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    private TaskService taskService;

    private Project project;

    private static final String TEAM = "unit";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskService = new TaskService(taskRepository, projectRepository);
        project = Project.builder().id(1L).name("Project").build();
    }

    @Test
    void createTask_shouldReturnSavedDTO() {
        TaskPayload.Create request = new TaskPayload.Create("Task", null, TaskStatus.TODO, TaskPriority.LOW, null);

        Task saved = Task.builder().id(10L).name("Task").status(TaskStatus.TODO).priority(TaskPriority.LOW)
                .project(project).build();

        when(projectRepository.findByIdAndTeamSlug(1L, TEAM)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskPayload.View dto = taskService.createTask(TEAM, 1L, request);
        assertThat(dto.id()).isEqualTo(saved.getId());
        assertThat(dto.name()).isEqualTo(saved.getName());
    }

    @Test
    void getTasksByProject_whenProjectMissing_shouldThrow() {
        when(projectRepository.findByIdAndTeamSlug(99L, TEAM)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.getTasksByProject(TEAM, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTaskById_whenIdsMismatch_shouldThrow() {
        Task task = Task.builder().id(3L).project(project).build();
        when(projectRepository.findByIdAndTeamSlug(1L, TEAM)).thenReturn(Optional.of(project));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));

        // Change project id mismatch; using wrong id
        assertThatThrownBy(() -> taskService.getTaskById(TEAM, 2L, 3L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTask_shouldInvokeRepositoryDelete() {
        Task task = Task.builder().id(3L).project(project).build();
        when(projectRepository.findByIdAndTeamSlug(1L, TEAM)).thenReturn(Optional.of(project));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));

        taskService.deleteTask(TEAM, 1L, 3L);
        verify(taskRepository).delete(task);
    }

    @Test
    void getTasksByProject_shouldReturnDTOs() {
        when(projectRepository.findByIdAndTeamSlug(1L, TEAM)).thenReturn(Optional.of(project));
        List<Task> tasks = List.of(
                Task.builder().id(1L).name("T1").status(TaskStatus.TODO).priority(TaskPriority.HIGH).project(project)
                        .build(),
                Task.builder().id(2L).name("T2").status(TaskStatus.TODO).priority(TaskPriority.HIGH).project(project)
                        .build());
        when(taskRepository.findByProjectId(1L)).thenReturn(tasks);

        List<TaskPayload.View> list = taskService.getTasksByProject(TEAM, 1L);
        assertThat(list).hasSize(2);
        assertThat(list).extracting("name").containsExactlyInAnyOrder("T1", "T2");
    }
}
