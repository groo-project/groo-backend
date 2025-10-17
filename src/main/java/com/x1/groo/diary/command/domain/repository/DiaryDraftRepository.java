package com.x1.groo.diary.command.domain.repository;

import com.x1.groo.diary.command.domain.dto.DiaryDraftListResponseDTO;
import com.x1.groo.diary.command.domain.entity.DiaryDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryDraftRepository extends JpaRepository<DiaryDraft, Integer> {
    Optional<DiaryDraft> findByUserIdAndDiaryDateAndForestId(
            @Param("userId") int userId,
            @Param("date") LocalDate date,
            @Param("forestId") int forestId
    );

    int countByUserIdAndForestId(int userId, int forestId);

    Optional<DiaryDraft> findByIdAndUserId(int diaryDraftId, int userId);

    @Query("SELECT new com.x1.groo.diary.command.domain.dto.DiaryDraftListResponseDTO(d.id, d.content, d.diaryDate)" +
            "FROM DiaryDraft d WHERE d.userId = :userId AND d.forestId = :forestId ORDER BY d.diaryDate DESC")
    List<DiaryDraftListResponseDTO> findAllByUserIdAndForestId(
            @Param("userId") int userId,
            @Param("forestId") int forestId
    );
}
