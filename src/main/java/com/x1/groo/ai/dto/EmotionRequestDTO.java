package com.x1.groo.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 감정 분석 API 호출 시 요청 바디를 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionRequestDTO {
    /**
     * 분석할 일기 텍스트
     */
    private String diary;
}