package com.example.projective.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public sealed interface TeamPayload permits TeamPayload.Create, TeamPayload.View {

    record Create(
            @NotBlank String name,
            @NotBlank @Pattern(regexp = "[a-z0-9-]+") String slug) implements TeamPayload {
    }

    record View(
            Long id,
            String name,
            String slug) implements TeamPayload {
    }
}
