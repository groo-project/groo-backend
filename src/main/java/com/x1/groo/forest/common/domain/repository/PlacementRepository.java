package com.x1.groo.forest.common.domain.repository;

import com.x1.groo.forest.common.domain.aggregate.PlacementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlacementRepository extends JpaRepository<PlacementEntity, Integer> {
    void deleteByForestItemIdIn(List<Integer> forestItemIds);

//    List<PlacementEntity> findByForestIdAndUserId(int forestId, int userId);
}
