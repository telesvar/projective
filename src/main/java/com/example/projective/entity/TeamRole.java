package com.example.projective.entity;

/**
 * Team-scoped roles sorted by ascending privilege.
 */
public enum TeamRole {

    /** Read-only member who can only view items inside the team. */
    VIEWER(0),

    /** Regular contributor – can create and modify Tasks / Issues. */
    MEMBER(1),

    /** Manager of a team (can create workspace / projects, invite members). */
    ADMIN(2),

    /** Team owner – has full control including deleting the team. */
    OWNER(3);

    private final int level;

    TeamRole(int level) {
        this.level = level;
    }

    /**
     * Returns {@code true} if this role's privilege is equal or higher than
     * {@code required}.
     */
    public boolean atLeast(TeamRole required) {
        return this.level >= required.level;
    }
}
