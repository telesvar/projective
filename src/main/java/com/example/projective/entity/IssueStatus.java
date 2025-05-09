package com.example.projective.entity;

public enum IssueStatus {

    TODO(0),

    IN_PROGRESS(1),

    IN_REVIEW(2),

    DONE(3);

    private final int order;

    IssueStatus(int order) {
        this.order = order;
    }

    /**
     * Allows staying in the same state or moving to an adjacent state in either
     * direction.
     * Prevents skipping more than one step to keep workflow integrity.
     */
    public boolean canTransitionTo(IssueStatus target) {
        return Math.abs(target.order - this.order) <= 1;
    }
}
