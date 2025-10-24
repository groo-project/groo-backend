package com.x1.groo.forest.mate.command.domain.repository;

import com.x1.groo.forest.mate.command.domain.aggregate.SharedForestEntity;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedForestRepository extends JpaRepository<SharedForestEntity, Integer> {


    boolean existsByUserIdAndForestId(int userId, int forestId);

    int countByForestId(int forestId);

    void deleteByUserIdAndForestId(int userId, int id);

    @Query("select count(sf) from SharedForestEntity sf where sf.forestId = :forestId")
    int countByForestIdForUpdate(@Param("forestId") int forestId);

    void deleteByUserId(int userId);

    List<SharedForestEntity> findAllByForestIdOrderByIdAsc(int forestId);

    List<SharedForestEntity> findByForestId(int forestId);
}
