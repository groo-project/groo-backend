package com.x1.groo.forest.emotion.command.domain.repository;

import com.x1.groo.forest.emotion.command.domain.aggregate.PlacementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlacementRepository extends JpaRepository<PlacementEntity, Integer> {
    void deleteByUserItemIdIn(List<Integer> userItemIds);

//    List<PlacementEntity> findByForestIdAndUserId(int forestId, int userId);
}
