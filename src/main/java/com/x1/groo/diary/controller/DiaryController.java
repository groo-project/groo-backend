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

import java.util.List;


@Tag(name = "일기", description = "일기 작성, 수정, 임시저장 및 조회 기능을 제공합니다.")
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
        int userId = user.getUserId();
        DiaryResponseDTO response = diaryService.createDiary(req, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일기 임시저장 등록")
    @PostMapping("/save")
    public ResponseEntity<DiarySaveResponseDTO> save(
            @RequestBody DiarySaveRequestDTO req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int userId = user.getUserId();
        DiarySaveResponseDTO response = diaryService.saveDiary(req, userId);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "일기 저장 조회")
    @GetMapping("/save")
    public ResponseEntity<List<DiarySaveInfoDTO>> getSaves(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int userId = user.getUserId();
        return ResponseEntity.ok(diaryService.getSaves(userId));
    }

    @Operation(summary = "일기 임기저장 상세 조회")
    @GetMapping("/save/{diaryId}")
    public ResponseEntity<DiarySaveDetailDTO> getSaveDetail(
            @PathVariable int diaryId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int userId = user.getUserId();
        DiarySaveDetailDTO detail = diaryService.getSaveDetail(userId, diaryId);
        return ResponseEntity.ok(detail);
    }

    @Operation(summary = "임시 저장된 일기 수정")
    @PutMapping("/save/{diaryId}")
    public ResponseEntity<DiarySaveUpdateResponseDTO> updateSave(
            @PathVariable int diaryId,
            @RequestBody DiarySaveRequestDTO req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int userId = user.getUserId();
        DiarySaveUpdateResponseDTO resp = diaryService.updateSave(userId, diaryId, req);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "임시 저장된 일기 삭제")
    @DeleteMapping("/save/{diaryId}")
    public ResponseEntity<Void> deleteSave(
            @PathVariable int diaryId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int userId = user.getUserId();
        diaryService.deleteSave(userId, diaryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "임시 저장된 일기 등록")
    @PostMapping("/save/{diaryId}/publish")
    public ResponseEntity<DiaryResponseDTO> publishSave(
            @PathVariable int diaryId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int userId = user.getUserId();
        return ResponseEntity.ok(diaryService.publishSave(userId, diaryId));
    }

    @Operation(summary = "일기 수정")
    @PutMapping("/edit")
    public ResponseEntity<DiaryUpdateResponseDTO> edit(
            @RequestBody DiaryUpdateRequestDTO req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int userId = user.getUserId();

        DiaryUpdateResponseDTO updatedDiary = diaryService.updateDiary(req, userId);
        return ResponseEntity.ok(updatedDiary);
    }
}
