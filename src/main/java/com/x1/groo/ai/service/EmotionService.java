package com.x1.groo.ai.service;

import com.x1.groo.ai.dto.EmotionRequestDTO;
import com.x1.groo.ai.dto.EmotionResponseDTO;

/**
 * AI 감정 분석 기능을 제공하는 서비스 인터페이스
 */
public interface EmotionService {

    /**
     * 사용자의 일기 내용을 바탕으로 감정 분석을 수행하고,
     * 분석 결과를 {@link EmotionResponseDTO} 형태로 반환한다.
     *
     * @param request 분석할 일기 텍스트를 담은 DTO
     * @return 감정 분석 결과 DTO
     */
    EmotionResponseDTO analyzeEmotion(EmotionRequestDTO request);
}