package com.example.projective.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import com.example.projective.entity.TaskPriority;
import com.example.projective.entity.TaskStatus;

public sealed interface TaskPayload permits TaskPayload.Create, TaskPayload.View {

    record Create(
            @NotBlank @Size(max = 255) String name,
            String description,
            @NotNull TaskStatus status,
            @NotNull TaskPriority priority,
            LocalDate dueDate) implements TaskPayload {
    }

    record View(
            Long id,
            String name,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Long projectId) implements TaskPayload {
    }
}
