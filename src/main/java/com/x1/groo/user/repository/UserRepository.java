package com.x1.groo.user.repository;

import com.x1.groo.user.aggregate.UserEntity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmailOrNickname(String email, String nickname);

    boolean existsByNickname(String nickname);

    Optional<UserEntity> findByOauthProviderAndOauthId(String provider, String id);

    @Query("SELECT u.nickname FROM UserEntity u WHERE u.nickname LIKE CONCAT(:baseNickname, '%')")
    List<String> findNicknamesByBase(@Param("baseNickname") String baseNickname);
}
