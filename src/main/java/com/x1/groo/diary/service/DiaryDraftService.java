package com.x1.groo.diary.service;

import com.x1.groo.diary.dto.DiaryDraftListResponseDTO;
import com.x1.groo.diary.dto.DiaryDraftRequestDTO;
import com.x1.groo.diary.dto.DiaryDraftUpdateRequestDTO;

import java.time.LocalDate;
import java.util.List;

public interface DiaryDraftService {
    boolean existsDraftByDate(int userId, LocalDate date);

    int getTotalDraftCount(int userId);

    void saveDraft(int userId, DiaryDraftRequestDTO req);

    void updateDraft(int userId, DiaryDraftUpdateRequestDTO req);

    List<DiaryDraftListResponseDTO> getDrafts(int userId);

    void deleteDraft(int userId, int diaryDraftId);
}
