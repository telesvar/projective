package com.example.projective.payload;

import com.example.projective.entity.IssueStatus;
import com.example.projective.entity.IssueType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public sealed interface IssuePayload permits IssuePayload.Create, IssuePayload.View {

    record Create(
            @NotBlank @Size(max = 255) String title,
            String description,
            @NotNull IssueType type,
            Integer points,
            Long parentId,
            IssueStatus status) implements IssuePayload {
    }

    record View(
            Long id,
            String title,
            String description,
            IssueType type,
            IssueStatus status,
            Integer points,
            Long parentId,
            Long projectId) implements IssuePayload {
    }
}
