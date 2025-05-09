package com.example.projective.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.projective.entity.Project;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByWorkspaceSlugAndWorkspaceTeamSlug(String workspaceSlug, String teamSlug);

    Optional<Project> findByIdAndWorkspaceSlugAndWorkspaceTeamSlug(Long id, String workspaceSlug, String teamSlug);
}
