package com.example.projective.payload;

import jakarta.validation.constraints.NotBlank;

public sealed interface AuthPayload permits AuthPayload.SignIn, AuthPayload.SignUp, AuthPayload.Token {

    record SignIn(
            @NotBlank String username,
            @NotBlank String password) implements AuthPayload {
    }

    record SignUp(
            @NotBlank String username,
            @NotBlank String password) implements AuthPayload {
    }

    record Token(String token) implements AuthPayload {
    }
}
