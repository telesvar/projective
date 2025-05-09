package com.example.projective.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public sealed interface WorkspacePayload permits WorkspacePayload.Create, WorkspacePayload.View {

    record Create(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Pattern(regexp = "[a-z0-9-]+") @Size(max = 100) String slug)
            implements WorkspacePayload {
    }

    record View(
            Long id,
            String name,
            String slug) implements WorkspacePayload {
    }
}
