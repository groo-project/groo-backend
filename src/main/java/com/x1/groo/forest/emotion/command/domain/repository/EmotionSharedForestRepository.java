package com.x1.groo.forest.emotion.command.domain.repository;

import com.x1.groo.forest.emotion.command.domain.aggregate.EmotionSharedForestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionSharedForestRepository extends JpaRepository<EmotionSharedForestEntity, Integer> {
    /**
     * userId와 forestId 조합이 shared_forest 테이블에 존재하는지 확인
     */
    boolean existsByUserIdAndForestId(int userId, int forestId);
}