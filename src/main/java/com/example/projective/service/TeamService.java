package com.example.projective.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projective.entity.Team;
import com.example.projective.entity.TeamMembership;
import com.example.projective.entity.TeamRole;
import com.example.projective.entity.User;
import com.example.projective.exception.ResourceAlreadyExistsException;
import com.example.projective.exception.ResourceNotFoundException;
import com.example.projective.payload.TeamPayload;
import com.example.projective.repository.TeamMembershipRepository;
import com.example.projective.repository.TeamRepository;
import com.example.projective.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMembershipRepository membershipRepository;

    public TeamPayload.View create(TeamPayload.Create dto, String username) {
        if (teamRepository.findBySlug(dto.slug()).isPresent()) {
            throw new ResourceAlreadyExistsException("Team slug already exists: " + dto.slug());
        }
        Team team = Team.builder().name(dto.name()).slug(dto.slug()).build();
        Team saved = teamRepository.save(team);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        TeamMembership membership = TeamMembership.builder()
                .team(saved)
                .user(user)
                .role(TeamRole.OWNER)
                .build();
        membershipRepository.save(membership);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamPayload.View> listMine(String username) {
        return membershipRepository
                .findByUserUsernameAndRoleIn(username, List.of(TeamRole.VIEWER, TeamRole.MEMBER, TeamRole.ADMIN, TeamRole.OWNER))
                .stream()
                .map(m -> toView(m.getTeam()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamPayload.View getBySlug(String slug) {
        Team team = teamRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + slug));
        return toView(team);
    }

    private TeamPayload.View toView(Team entity) {
        return new TeamPayload.View(entity.getId(), entity.getName(), entity.getSlug());
    }
}
