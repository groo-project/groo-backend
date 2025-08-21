package com.x1.groo.auth.command.domain.repository;

import com.x1.groo.auth.command.application.aggregate.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    // db 에서 jti_hash 찾기
    Optional<RefreshToken> findByJtiHash(String s);

    void deleteAllByUserId(int userId);

    Optional<RefreshToken> findByUserId(int subjectUserId);
}
