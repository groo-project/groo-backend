package com.x1.groo.diary.repository;

import com.x1.groo.diary.dto.DiaryDraftListResponseDTO;
import com.x1.groo.diary.entity.DiaryDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryDraftRepository extends JpaRepository<DiaryDraft, Integer> {
    Optional<DiaryDraft> findByUserIdAndDiaryDate(@Param("userId") int userId, @Param("date") LocalDate date);

    int countByUserId(int userId);

    Optional<DiaryDraft> findByIdAndUserId(int diaryDraftId, int userId);

    @Query("SELECT new com.x1.groo.diary.dto.DiaryDraftListResponseDTO(d.id, d.content, d.diaryDate)" +
            "FROM DiaryDraft d WHERE d.userId = :userId ORDER BY d.diaryDate DESC")
    List<DiaryDraftListResponseDTO> findAllByUserId(@Param("userId") int userId);
}
