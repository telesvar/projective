package com.example.projective.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projective.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
