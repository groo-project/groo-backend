package com.x1.groo.diary.service;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.diary.dto.DiaryDraftInfoResponseDTO;
import com.x1.groo.diary.dto.DiaryDraftListResponseDTO;
import com.x1.groo.diary.dto.DiaryDraftRequestDTO;
import com.x1.groo.diary.dto.DiaryDraftUpdateRequestDTO;
import com.x1.groo.diary.entity.DiaryDraft;
import com.x1.groo.diary.repository.DiaryDraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryDraftServiceImpl implements DiaryDraftService {

    private final DiaryDraftRepository diaryDraftRepository;

    @Override
    @Transactional
    public DiaryDraftInfoResponseDTO existsDraftByDate(int userId, LocalDate date) {
        return diaryDraftRepository.findIdByUserIdAndDiaryDate(userId, date)
                .map(d -> new DiaryDraftInfoResponseDTO(
                        d.getId(),
                        d.getContent(),
                        d.getDiaryDate()
                )).orElse(new DiaryDraftInfoResponseDTO(null, null, null));
    }

    @Override
    @Transactional
    public int getTotalDraftCount(int userId) {
        return diaryDraftRepository.countByUserId(userId);
    }

    @Override
    @Transactional
    public void saveDraft(int userId, DiaryDraftRequestDTO req) {
        DiaryDraft draft = DiaryDraft.builder()
                .userId(userId)
                .diaryDate(req.getDate())
                .content(req.getContent())
                .build();

        diaryDraftRepository.save(draft);
    }

    @Override
    @Transactional
    public void updateDraft(int userId, DiaryDraftUpdateRequestDTO req) {
        DiaryDraft draft = diaryDraftRepository.findByIdAndUserId(req.getDiaryDraftId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_DRAFT_NOT_FOUND));

        draft.setContent(req.getContent());
    }

    @Override
    @Transactional
    public List<DiaryDraftListResponseDTO> getDrafts(int userId) {
        return diaryDraftRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteDraft(int userId, int diaryDraftId) {
        DiaryDraft draft = diaryDraftRepository.findByIdAndUserId(diaryDraftId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_DRAFT_NOT_FOUND));

        diaryDraftRepository.delete(draft);
    }
}
