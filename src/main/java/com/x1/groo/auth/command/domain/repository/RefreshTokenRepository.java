package com.x1.groo.auth.command.domain.repository;

import com.x1.groo.auth.command.application.aggregate.RefreshToken;
import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;
import org.checkerframework.checker.interning.qual.CompareToMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.parameters.P;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    // db 에서 jti_hash 찾기
    Optional<RefreshToken> findByJtiHash(String s);

    void deleteAllByUserId(int userId);

    Optional<RefreshToken> findByUserId(int subjectUserId);


    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO refresh_token (user_id, jti_hash, expires_at)
        VALUES (:userId, :newJti, :expiresAt)
        ON DUPLICATE KEY UPDATE
            jti_hash = VALUES(jti_hash),
            expires_at = VALUES(expires_at)
    """, nativeQuery = true)
    void updateRefreshToken(@Param("userId") int userId,
                            @Param("newJti") String newJti,
                            @Param("expiresAt") LocalDateTime expiresAt);
}
