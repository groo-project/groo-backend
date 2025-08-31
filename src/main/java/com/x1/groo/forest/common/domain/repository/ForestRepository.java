package com.x1.groo.forest.common.domain.repository;

import com.x1.groo.forest.common.domain.aggregate.ForestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("select f.id from ForestEntity f where f.user.id = :userId order by 1 ASC \n" +
            "       limit 1")
    int findActiveForestIdByUserId(@Param("userId") int id);

}
