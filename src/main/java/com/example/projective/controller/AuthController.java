package com.example.projective.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.projective.entity.User;
import com.example.projective.entity.UserRole;
import com.example.projective.payload.AuthPayload;
import com.example.projective.repository.UserRepository;
import com.example.projective.security.JwtTokenProvider;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/sign-in")
    public ResponseEntity<AuthPayload.Token> signIn(@Valid @RequestBody AuthPayload.SignIn request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String token = tokenProvider.generateToken(
                (UserDetails) authentication.getPrincipal());
        return ResponseEntity.ok(new AuthPayload.Token(token));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthPayload.Token> signUp(@Valid @RequestBody AuthPayload.SignUp request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().build();
        }
        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(UserRole.USER))
                .build();
        userRepository.save(user);

        String token = tokenProvider.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles("USER")
                        .build());
        return ResponseEntity.ok(new AuthPayload.Token(token));
    }
}
