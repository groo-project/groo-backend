package com.x1.groo.forest.mate.command.domain.repository;

import com.x1.groo.forest.mate.command.domain.aggregate.ForestInviteEntity;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ForestInviteRepository extends JpaRepository<ForestInviteEntity, Integer> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ForestInviteEntity f WHERE f.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") LocalDateTime cutoff);

    ForestInviteEntity findByCode(String inviteCode);

    Optional<ForestInviteEntity> findByForestId(int forestId);
}
