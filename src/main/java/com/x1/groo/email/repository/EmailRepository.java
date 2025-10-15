package com.x1.groo.email.repository;

import com.x1.groo.email.aggregate.EmailEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface EmailRepository extends JpaRepository<EmailEntity, Integer> {

    @Query("""
        SELECT e
          FROM EmailEntity e
         WHERE e.email = :email
           AND e.isVerified = false
           AND e.expiresAt > CURRENT_TIMESTAMP
         ORDER BY e.createdAt desc
        """)
    EmailEntity findByEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM EmailEntity e WHERE e.expiresAt < :cutoff AND e.isVerified = false")
    int deleteExpired(@Param("cutoff") LocalDateTime cutoff);

    void deleteAllByEmail(@Email @NotEmpty(message = "이메일을 입력해 주세요") String email);

    EmailEntity findFirstByEmailOrderByCreatedAtDesc(String email);
}
