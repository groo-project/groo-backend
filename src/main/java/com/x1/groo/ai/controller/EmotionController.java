package com.x1.groo.ai.controller;

import com.x1.groo.ai.dto.EmotionRequestDTO;
import com.x1.groo.ai.dto.EmotionResponseDTO;
import com.x1.groo.ai.service.EmotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 감정 분석 API 엔드포인트를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/ai")
public class EmotionController {

    private final EmotionService emotionService;

    public EmotionController(EmotionService emotionService) {
        this.emotionService = emotionService;
    }

    /**
     * POST /api/ai/analyze-diary
     * 요청 바디의 일기를 분석하여 감정 결과를 반환한다.
     */
    @PostMapping("/analyze-diary")
    public ResponseEntity<EmotionResponseDTO> analyze(@RequestBody EmotionRequestDTO request) {
        if (request.getDiary() == null || request.getDiary().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        EmotionResponseDTO response = emotionService.analyzeEmotion(request);
        return ResponseEntity.ok(response);
    }
}
