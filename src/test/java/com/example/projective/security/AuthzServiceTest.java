package com.example.projective.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.projective.entity.TeamMembership;
import com.example.projective.entity.TeamRole;
import com.example.projective.repository.TeamMembershipRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AuthzServiceTest {

    @Mock
    private TeamMembershipRepository membershipRepository;

    private AuthzService authzService;

    private final String teamSlug = "team-a";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authzService = new AuthzService(membershipRepository);
    }

    private UsernamePasswordAuthenticationToken auth(String username, String... roles) {
        return new UsernamePasswordAuthenticationToken(
                username,
                "pwd",
                java.util.Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());
    }

    @Test
    void serviceAdmin_bypassesAnyCheck() {
        var auth = auth("bob", "SERVICE_ADMIN");
        boolean allowed = authzService.hasTeamRoleAtLeast(teamSlug, auth, TeamRole.OWNER);
        assertThat(allowed).isTrue();
    }

    @Test
    void userWithExactRole_isAllowed() {
        var auth = auth("alice");
        when(membershipRepository.findByUserUsernameAndTeamSlug(eq("alice"), eq(teamSlug)))
                .thenReturn(Optional.of(TeamMembership.builder().role(TeamRole.ADMIN).build()));
        boolean allowed = authzService.hasTeamRoleAtLeast(teamSlug, auth, TeamRole.MEMBER);
        assertThat(allowed).isTrue();
    }

    @Test
    void insufficientRole_isDenied() {
        var auth = auth("john");
        when(membershipRepository.findByUserUsernameAndTeamSlug(eq("john"), eq(teamSlug)))
                .thenReturn(Optional.of(TeamMembership.builder().role(TeamRole.MEMBER).build()));
        boolean allowed = authzService.hasTeamRoleAtLeast(teamSlug, auth, TeamRole.ADMIN);
        assertThat(allowed).isFalse();
    }
}
