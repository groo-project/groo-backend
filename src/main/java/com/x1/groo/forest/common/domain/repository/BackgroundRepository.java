package com.x1.groo.forest.common.domain.repository;

import com.x1.groo.forest.common.domain.aggregate.BackgroundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackgroundRepository extends JpaRepository<BackgroundEntity, Integer> {
}
