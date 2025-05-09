package com.example.projective.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projective.entity.Project;
import com.example.projective.entity.Task;
import com.example.projective.exception.ResourceNotFoundException;
import com.example.projective.payload.TaskPayload;
import com.example.projective.repository.ProjectRepository;
import com.example.projective.repository.TaskRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskPayload.View createTask(String teamSlug, Long projectId, TaskPayload.Create dto) {
        Project project = projectRepository.findByIdAndTeamSlug(projectId, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId + " in team " + teamSlug));
        Task task = new Task();
        task.setName(dto.name());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setPriority(dto.priority());
        task.setDueDate(dto.dueDate());
        task.setProject(project);
        Task saved = taskRepository.save(task);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskPayload.View> getTasksByProject(String teamSlug, Long projectId) {
        Project project = projectRepository.findByIdAndTeamSlug(projectId, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId + " in team " + teamSlug));
        return taskRepository.findByProjectId(project.getId()).stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskPayload.View getTaskById(String teamSlug, Long projectId, Long taskId) {
        Project project = projectRepository.findByIdAndTeamSlug(projectId, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId + " in team " + teamSlug));
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getProject().getId().equals(project.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id " + taskId + " for project " + projectId));
        return toView(task);
    }

    public TaskPayload.View updateTask(String teamSlug, Long projectId, Long taskId, TaskPayload.Create dto) {
        Project project = projectRepository.findByIdAndTeamSlug(projectId, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId + " in team " + teamSlug));
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getProject().getId().equals(project.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id " + taskId + " for project " + projectId));
        if (dto.name() != null)
            task.setName(dto.name());
        task.setDescription(dto.description());
        if (dto.status() != null)
            task.setStatus(dto.status());
        if (dto.priority() != null)
            task.setPriority(dto.priority());
        task.setDueDate(dto.dueDate());
        Task updated = taskRepository.save(task);
        return toView(updated);
    }

    public void deleteTask(String teamSlug, Long projectId, Long taskId) {
        Project project = projectRepository.findByIdAndTeamSlug(projectId, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId + " in team " + teamSlug));
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getProject().getId().equals(project.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id " + taskId + " for project " + projectId));
        taskRepository.delete(task);
    }

    private TaskPayload.View toView(Task t) {
        return new TaskPayload.View(t.getId(), t.getName(), t.getDescription(), t.getStatus(), t.getPriority(),
                t.getDueDate(), t.getProject() != null ? t.getProject().getId() : null);
    }
}
