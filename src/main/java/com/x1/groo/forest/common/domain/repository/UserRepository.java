package com.x1.groo.forest.common.domain.repository;

import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("TempUserRepository")
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
}
