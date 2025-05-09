package com.example.projective.payload;

import java.time.LocalDate;

import com.example.projective.entity.ProjectStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public sealed interface ProjectPayload permits ProjectPayload.Create, ProjectPayload.View {

    record Create(
            @NotBlank @Size(max = 255) String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            ProjectStatus status) implements ProjectPayload {
    }

    record View(
            Long id,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            ProjectStatus status) implements ProjectPayload {
    }
}
