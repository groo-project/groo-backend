package com.x1.groo.item.domain.storage.repository;

import com.x1.groo.item.domain.storage.aggregate.UserItemStorageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserItemStorageRepository extends JpaRepository<UserItemStorageEntity, Integer> {

    Optional<UserItemStorageEntity> findByUserIdAndItemIdAndForestId(int userId, int itemId, int forestId);
}
