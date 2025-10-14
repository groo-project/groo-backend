package com.x1.groo.forest.common.domain.repository;

import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import io.lettuce.core.Value;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("TempUserRepository")
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByOauthProviderAndOauthId(String google, String sub);

    Optional<UserEntity> findByEmail(String email);
}
