package com.x1.groo.diary.service;

import com.x1.groo.diary.dto.*;

/**
 * 사용자 ID를 파라미터로 받아 일기를 생성
 */
public interface DiaryService {
    /** 정식 등록 (AI 감정분석, 날씨, 상위2개 감정 저장) */
    DiaryResponseDTO createDiary(DiaryRequestDTO request, int userId);

    /** 일기 수정 **/
    DiaryUpdateResponseDTO updateDiary(DiaryUpdateRequestDTO request, int userId);
}
