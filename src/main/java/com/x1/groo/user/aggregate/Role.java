package com.x1.groo.user.aggregate;

public enum Role {
    COMMON("COMMON"),
    ADMIN("ADMIN");

    private final String role;

    Role(String role) {
        this.role = role;
    }
}
