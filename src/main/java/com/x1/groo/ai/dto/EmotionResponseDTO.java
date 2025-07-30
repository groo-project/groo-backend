package com.x1.groo.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 감정 분석 API 호출 후 반환할 응답 바디를 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionResponseDTO {
    /**
     * 감정별 백분율 결과
     * 예: { "즐거움": 40, "슬픔": 10, … }
     */
    private Map<String, Integer> emotionResult;

    /**
     * 가장 높은 비중을 차지한 메인 감정
     * 예: "즐거움"
     */
    private String mainEmotion;

    /**
     * 메인 감정에 매핑된 날씨
     * 예: "맑음"
     */
    private String weather;
}