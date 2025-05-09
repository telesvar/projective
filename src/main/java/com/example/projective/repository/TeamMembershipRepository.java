package com.example.projective.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projective.entity.TeamMembership;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {

    Optional<TeamMembership> findByUserUsernameAndTeamSlug(String username, String slug);

    List<TeamMembership> findByUserUsernameAndRoleIn(String username,
            Collection<com.example.projective.entity.TeamRole> roles);
}
