package com.x1.groo.diary.controller;

import com.x1.groo.common.JwtUtil;
import com.x1.groo.diary.dto.*;
import com.x1.groo.diary.service.DiaryService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "일기 API", description = "일기 작성, 수정, 임시저장 및 조회 기능을 제공하는 API 입니다.")
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "일기 등록")
    @PostMapping
    public ResponseEntity<DiaryResponseDTO> create(
            @RequestBody DiaryRequestDTO req,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // 1) "Bearer " 제거 및 토큰 파싱
        String token = authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : authorizationHeader.trim();

        // 2) JWT 파싱
        Claims claims = jwtUtil.parseJwt(token);

        // 3) userId 클레임 꺼내기
        int userId = claims.get("userId", Number.class).intValue();

        // 4) 서비스 호출 후 DTO 반환
        DiaryResponseDTO response = diaryService.createDiary(req, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일기 임시저장 등록")
    @PostMapping("/save")
    public ResponseEntity<DiarySaveResponseDTO> save(
            @RequestBody DiarySaveRequestDTO req,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader.trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = claims.get("userId", Number.class).intValue();

        DiarySaveResponseDTO response = diaryService.saveDiary(req, userId);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "일기 저장 조회")
    @GetMapping("/save")
    public ResponseEntity<List<DiarySaveInfoDTO>> getSaves(
            @RequestHeader("Authorization") String authHeader
    ) {
        int userId = extractUserId(authHeader);
        return ResponseEntity.ok(diaryService.getSaves(userId));
    }

    private int extractUserId(String authHeader) {
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader.trim();
        Claims claims = jwtUtil.parseJwt(token);
        return claims.get("userId", Number.class).intValue();
    }

    @Operation(summary = "일기 임기저장 상세 조회")
    @GetMapping("/save/{diaryId}")
    public ResponseEntity<DiarySaveDetailDTO> getSaveDetail(
            @PathVariable int diaryId,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader.trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = claims.get("userId", Number.class).intValue();

        DiarySaveDetailDTO detail = diaryService.getSaveDetail(userId, diaryId);
        return ResponseEntity.ok(detail);
    }

    @Operation(summary = "임시 저장된 일기 수정")
    @PutMapping("/save/{diaryId}")
    public ResponseEntity<DiarySaveUpdateResponseDTO> updateSave(
            @PathVariable int diaryId,
            @RequestBody DiarySaveRequestDTO req,
            @RequestHeader("Authorization") String authHeader
    ) {
        int userId = extractUserId(authHeader);
        DiarySaveUpdateResponseDTO resp = diaryService.updateSave(userId, diaryId, req);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "임시 저장된 일기 삭제")
    @DeleteMapping("/save/{diaryId}")
    public ResponseEntity<Void> deleteSave(
            @PathVariable int diaryId,
            @RequestHeader("Authorization") String authHeader
    ) {
        int userId = extractUserId(authHeader);
        diaryService.deleteSave(userId, diaryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "임시 저장된 일기 등록")
    @PostMapping("/save/{diaryId}/publish")
    public ResponseEntity<DiaryResponseDTO> publishSave(
            @PathVariable int diaryId,
            @RequestHeader("Authorization") String authHeader
    ) {
        int userId = extractUserId(authHeader);
        return ResponseEntity.ok(diaryService.publishSave(userId, diaryId));
    }

    @Operation(summary = "일기 수정")
    @PutMapping("/edit")
    public ResponseEntity<DiaryUpdateResponseDTO> edit(
            @RequestBody DiaryUpdateRequestDTO req,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader.trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = claims.get("userId", Number.class).intValue();

        DiaryUpdateResponseDTO updatedDiary = diaryService.updateDiary(req, userId);
        return ResponseEntity.ok(updatedDiary);
    }
}
