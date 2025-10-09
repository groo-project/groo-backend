package com.x1.groo.forest.common.domain.repository;

import com.x1.groo.forest.common.domain.aggregate.ForestEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("TempForestRepository")
public interface ForestRepository extends JpaRepository<ForestEntity, Integer> {
    boolean existsByIdAndUserId(int id, int userId);

    // 공유된 숲에 userId가 속해 있는지 여부 확인
    @Query("SELECT COUNT(sf) > 0 FROM SharedForestEntity sf WHERE sf.userId = :userId AND sf.forestId = :forestId")
    boolean isUserInSharedForest(@Param("userId") int userId, @Param("forestId") int forestId);

//    @Query(value = "select f.id from ForestEntity f where f.user.id = :userId order by f.id ASC LIMIT 1")
    @Query(value = """
      select f.id
      from forest f
      where f.user_id = :userId
      order by f.id asc
      limit 1
    """, nativeQuery = true)
    int findActiveForestIdByUserId(@Param("userId") int id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from ForestEntity f where f.id = :id")
    ForestEntity lockById(@Param("id") int forestId);

    Optional<ForestEntity> findByUser_Email(String email);
}
