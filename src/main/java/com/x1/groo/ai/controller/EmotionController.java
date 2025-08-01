package com.x1.groo.ai.controller;

import com.x1.groo.ai.dto.EmotionRequestDTO;
import com.x1.groo.ai.dto.EmotionResponseDTO;
import com.x1.groo.ai.service.EmotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "감정 분석" , description = "AI를 이용하여 감정을 분석해 반환하는 기능을 제공합니다.")
@RestController
@RequestMapping("/api/ai")
public class EmotionController {

    private final EmotionService emotionService;

    public EmotionController(EmotionService emotionService) {
        this.emotionService = emotionService;
    }

    @Operation(summary = "일기 감정 분석 요청", description = "일기를 분석하여 감정 결과를 반환합니다.")
    @PostMapping("/analyze-diary")
    public ResponseEntity<EmotionResponseDTO> analyze(@RequestBody EmotionRequestDTO request) {
        if (request.getDiary() == null || request.getDiary().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        EmotionResponseDTO response = emotionService.analyzeEmotion(request);
        return ResponseEntity.ok(response);
    }
}
