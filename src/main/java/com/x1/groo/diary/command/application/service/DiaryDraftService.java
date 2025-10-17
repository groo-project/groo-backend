package com.x1.groo.diary.command.application.service;

import com.x1.groo.diary.command.domain.dto.DiaryDraftInfoResponseDTO;
import com.x1.groo.diary.command.domain.dto.DiaryDraftListResponseDTO;
import com.x1.groo.diary.command.domain.dto.DiaryDraftRequestDTO;
import com.x1.groo.diary.command.domain.dto.DiaryDraftUpdateRequestDTO;

import java.time.LocalDate;
import java.util.List;

public interface DiaryDraftService {
    DiaryDraftInfoResponseDTO existsDraftByDate(int userId, LocalDate date, int forestId);

    int getTotalDraftCount(int userId, int forestId);

    void saveDraft(int userId, DiaryDraftRequestDTO req);

    void updateDraft(int userId, DiaryDraftUpdateRequestDTO req);

    List<DiaryDraftListResponseDTO> getDrafts(int userId, int forestId);

    void deleteDraft(int userId, int diaryDraftId);
}
