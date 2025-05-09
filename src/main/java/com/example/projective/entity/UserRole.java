package com.example.projective.entity;

public enum UserRole {
    /**
     * A regular account with access only to the teams it belongs to (scoped by
     * memberships).
     */
    USER,

    /**
     * A system-wide operator role with unrestricted access across all tenants.
     * <p>
     * This is intentionally named <code>SERVICE_ADMIN</code> (instead of just
     * <code>ADMIN</code>)
     * to avoid confusion with
     * {@link com.example.projective.entity.TeamRole#ADMIN ADMIN} which
     * is scoped <em>inside a single team</em>.
     * </p>
     */
    SERVICE_ADMIN,
}
