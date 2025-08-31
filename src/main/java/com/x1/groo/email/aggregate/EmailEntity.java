package com.x1.groo.email.aggregate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "email_verification")
public class EmailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, name = "verification_code")
    private String verificationCode;

    @Column(nullable = false, name = "is_verified")
    private boolean isVerified = false;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    void prePersist() {
        if(createdAt == null) createdAt = LocalDateTime.now();
        if(expiresAt == null) expiresAt = createdAt.plusMinutes(5);
    }
}
