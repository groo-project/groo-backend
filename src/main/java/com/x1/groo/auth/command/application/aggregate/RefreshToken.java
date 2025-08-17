package com.x1.groo.auth.command.application.aggregate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Table(name = "refresh_token")
@Getter
@Setter
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int userId;

    //    @Column(nullable = false, length = 128)
//    @Column(nullable = false)
//    private  String deviceId;

    @Column(nullable = false, length = 64, unique = true)
    private String jtiHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean isRevoked = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
