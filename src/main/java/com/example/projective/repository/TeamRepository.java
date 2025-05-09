package com.example.projective.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projective.entity.Team;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
