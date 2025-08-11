package com.x1.groo.forest.mate.command.domain.repository;

import com.x1.groo.forest.mate.command.domain.aggregate.SharedForestEntity;
import jakarta.persistence.LockModeType;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.parameters.P;

public interface SharedForestRepository extends JpaRepository<SharedForestEntity, Integer> {

    boolean existsByUserIdAndForestId(int userId, int forestId);

    int countByForestId(int forestId);

    void deleteByUserIdAndForestId(int userId, int id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select count(sf) from SharedForestEntity sf where sf.forestId = :forestId")
    int countByForestIdForUpdate(@Param("forestId") int forestId);
}
