package com.x1.groo.forest.mate.command.domain.repository;

import com.x1.groo.forest.mate.command.domain.aggregate.SharedForestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedForestRepository extends JpaRepository<SharedForestEntity, Integer> {
    boolean existsByUserIdAndForestId(int userId, int forestId);

    int countByForestId(int forestId);

    void deleteByUserIdAndForestId(int userId, int id);
}
