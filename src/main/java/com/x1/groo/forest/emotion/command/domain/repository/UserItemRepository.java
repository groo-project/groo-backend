package com.x1.groo.forest.emotion.command.domain.repository;

import com.x1.groo.forest.emotion.command.domain.aggregate.UserItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserItemRepository extends JpaRepository<UserItemEntity, Integer> {
    List<UserItemEntity> findByUserIdAndForestId(int userId, int forestId);

    Optional<UserItemEntity> findByUserIdAndItemIdAndForestId(int userId, int itemId, int forestId);
}
