package com.x1.groo.forest.common.domain.repository;

import com.x1.groo.forest.common.domain.aggregate.ForestItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ForestItemRepository extends JpaRepository<ForestItemEntity, Integer> {

    List<ForestItemEntity> findByForestId(int forestId);

    Optional<ForestItemEntity> findByItemIdAndForestId(int itemId, int forestId);
}
