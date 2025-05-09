package com.example.projective.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.projective.entity.TeamRole;
import com.example.projective.repository.TeamMembershipRepository;

@Component("authz")
@RequiredArgsConstructor
public class AuthzService {

    private final TeamMembershipRepository membershipRepository;

    /** Shorthand to detect a service-wide administrator (super-user). */
    private boolean isServiceAdmin(Authentication auth) {
        return auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE_ADMIN"));
    }

    /**
     * Returns {@code true} if the authenticated user has <em>at least</em> the
     * required team-scope role
     * or is a service administrator. This avoids the boilerplate of passing
     * multiple roles in
     * every {@literal @PreAuthorize} expression and follows the natural role
     * hierarchy defined in
     * {@link com.example.projective.entity.TeamRole#atLeast(TeamRole)}.
     */
    public boolean hasTeamRoleAtLeast(String teamSlug, Authentication auth, TeamRole requiredRole) {
        if (isServiceAdmin(auth)) {
            return true;
        }
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String username = auth.getName();
        return membershipRepository.findByUserUsernameAndTeamSlug(username, teamSlug)
                .map(membership -> membership.getRole().atLeast(requiredRole))
                .orElse(false);
    }

    /**
     * Legacy support – equivalent to asking "the member has one of these exact
     * roles".
     * Prefer {@link #hasTeamRoleAtLeast} going forward.
     */
    public boolean hasTeamRole(String teamSlug, Authentication auth, TeamRole... roles) {
        if (isServiceAdmin(auth)) {
            return true;
        }
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String username = auth.getName();
        return membershipRepository.findByUserUsernameAndTeamSlug(username, teamSlug)
                .map(m -> java.util.Arrays.asList(roles).contains(m.getRole()))
                .orElse(false);
    }
}
