package com.example.projective.entity;

/**
 * Team-scoped roles sorted by ascending privilege.
 */
public enum TeamRole {

    /** Basic read-only / contributor. */
    MEMBER(0),

    /** Manager of a team (can create workspace / projects, invite members). */
    ADMIN(1),

    /** Team owner – has full control including deleting the team. */
    OWNER(2);

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
