package com.example.projective.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.projective.entity.Workspace;

import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findBySlugAndTeamSlug(String slug, String teamSlug);

    boolean existsBySlugAndTeamSlug(String slug, String teamSlug);
}
