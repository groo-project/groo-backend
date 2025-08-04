package com.x1.groo.forest.mate.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.x1.groo.forest.mate.command.domain.aggregate.InviteCodeStatus.ACTIVE;

@Setter
@Entity
@Table(name = "forest_invite")
public class ForestInviteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteCodeStatus status;

    @Column(nullable = false, name = "created_by")
    private int createdBy;

    @Column(nullable = false, name = "forest_id")
    private int forestId;

    @PrePersist
    void prePersist() {
        if(createdAt == null) createdAt = LocalDateTime.now();
        if(status == null) status = ACTIVE;
        if(expiresAt == null) expiresAt = createdAt.plusHours(24L);
    }

}
