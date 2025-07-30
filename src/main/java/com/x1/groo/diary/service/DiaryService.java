package com.x1.groo.diary.service;

import com.x1.groo.diary.dto.*;

import java.util.List;

/**
 * 사용자 ID를 파라미터로 받아 일기를 생성
 */
public interface DiaryService {
    /** 정식 등록 (AI 감정분석, 날씨, 상위2개 감정 저장) */
    DiaryResponseDTO createDiary(DiaryRequestDTO request, int userId);
    /** 임시 저장 (AI 호출 없이 content만 저장) */
    DiarySaveResponseDTO saveDiary(DiarySaveRequestDTO request, int userId);

    /** 임시 저장된 일기 조회 */
    List<DiarySaveInfoDTO> getSaves(int userId);
    /** 임시 저장된 일기 상세 조회 */
    DiarySaveDetailDTO getSaveDetail(int userId, int diaryId);
    /** 임시 저장된 일기 수정 */
    DiarySaveUpdateResponseDTO updateSave(int userId, int diaryId, DiarySaveRequestDTO request);
    /** 임시 저장된 일기 삭제 */
    void deleteSave(int userId, int diaryId);
    /** 임시 저장된 일기를 등록 */
    DiaryResponseDTO publishSave(int userId, int diaryId);
    /** 일기 수정 **/
    DiaryUpdateResponseDTO updateDiary(DiaryUpdateRequestDTO request, int userId);
}