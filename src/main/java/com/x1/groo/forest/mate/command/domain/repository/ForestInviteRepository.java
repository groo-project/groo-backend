package com.x1.groo.forest.mate.command.domain.repository;

import com.x1.groo.forest.mate.command.domain.aggregate.ForestInviteEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ForestInviteRepository extends JpaRepository<ForestInviteEntity, Integer> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE forest_invite
       SET status = 'USED'
     WHERE id = :id
       AND status = 'ACTIVE'
       AND expires_at > NOW()
  """, nativeQuery = true)
    int consume(@Param("id") int id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ForestInviteEntity f WHERE f.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") LocalDateTime cutoff);
}
