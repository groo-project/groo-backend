package com.x1.groo.forest.mate.command.domain.aggregate;

public enum InviteCodeStatus {
    ACTIVE("ACTIVE"),
    REVOKED("REVOKED");
    private final String inviteCodeStatus;

    InviteCodeStatus(String inviteCodeStatus) {
        this.inviteCodeStatus = inviteCodeStatus;
    }
}
