package com.x1.groo.auth.command.domain.repository;

import com.x1.groo.auth.command.application.aggregate.RefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    // db 에서 jti_hash 찾기
    Optional<RefreshToken> findByJtiHash(String s);

    void deleteAllByUserId(int userId);

//    Optional<RefreshToken> findByUserId(int subjectUserId);


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


    @Query("SELECT r FROM RefreshToken r WHERE r.userId = :userId ORDER BY r.expiresAt DESC")
    List<RefreshToken> findAllByUserId(@Param("userId") int userId);
}
