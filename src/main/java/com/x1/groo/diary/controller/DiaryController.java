package com.x1.groo.diary.controller;

import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.diary.dto.*;
import com.x1.groo.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



@Tag(name = "일기", description = "일기 작성, 수정, 조회 기능을 제공합니다.")
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(summary = "일기 등록")
    @PostMapping
    public ResponseEntity<DiaryResponseDTO> create(
            @RequestBody DiaryRequestDTO req,
            @AuthenticationPrincipal CustomUserDetails user
            ) {
        DiaryResponseDTO response = diaryService.createDiary(req, user.getUserId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일기 수정")
    @PutMapping
    public ResponseEntity<DiaryUpdateResponseDTO> edit(
            @RequestBody DiaryUpdateRequestDTO req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int userId = user.getUserId();

        DiaryUpdateResponseDTO updatedDiary = diaryService.updateDiary(req, userId);
        return ResponseEntity.ok(updatedDiary);
    }
}
