package com.x1.groo.forest.mate.command.domain.repository;

import com.x1.groo.forest.mate.command.domain.aggregate.ForestInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForestInviteRepository extends JpaRepository<ForestInviteEntity, Integer> {
}
