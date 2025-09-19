package com.x1.groo.diary.controller;

import com.x1.groo.diary.dto.DiaryDraftInfoResponseDTO;
import com.x1.groo.diary.dto.DiaryDraftListResponseDTO;
import com.x1.groo.diary.dto.DiaryDraftRequestDTO;
import com.x1.groo.diary.dto.DiaryDraftUpdateRequestDTO;
import com.x1.groo.diary.service.DiaryDraftService;
import com.x1.groo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "일기 임시 저장", description = "일기 임시 저장 작성, 조회, 수정, 삭제 기능을 제공합니다.")
@RestController
@RequestMapping("/api/diaries/drafts")
@RequiredArgsConstructor
public class DiaryDraftController {

    private final DiaryDraftService diaryDraftService;

    @Operation(summary = "해당 날짜 일기 임시 저장 여부")
    @GetMapping("/{date}")
    public ResponseEntity<DiaryDraftInfoResponseDTO> isDraftExist(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
            ) {
        DiaryDraftInfoResponseDTO draft = diaryDraftService.existsDraftByDate(user.getUserId(), date);

        return ResponseEntity.ok(draft);
    }

    @Operation(summary = "총 임시 저장 개수 반환")
    @GetMapping("/count")
    public ResponseEntity<Integer> getDraftCount(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int count = diaryDraftService.getTotalDraftCount(user.getUserId());

        return ResponseEntity.ok(count);
    }

    @Operation(summary = "일기 임시 저장 생성")
    @PostMapping
    public ResponseEntity<Void> saveDiaryDraft(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody DiaryDraftRequestDTO req
            ) {
        diaryDraftService.saveDraft(user.getUserId(), req);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "일기 임시 저장 수정")
    @PutMapping
    public ResponseEntity<Void> updateDiaryDraft(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody DiaryDraftUpdateRequestDTO req
            ) {
        diaryDraftService.updateDraft(user.getUserId(), req);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "일기 임시 저장 목록 조회")
    @GetMapping
    public ResponseEntity<List<DiaryDraftListResponseDTO>> getDrafts(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<DiaryDraftListResponseDTO> drafts = diaryDraftService.getDrafts(user.getUserId());

        return ResponseEntity.ok(drafts);
    }

    @Operation(summary = "일기 임시 저장 삭제")
    @DeleteMapping("/{diaryDraftId}")
    public ResponseEntity<Void> deleteDraft(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int diaryDraftId
    ) {
        diaryDraftService.deleteDraft(user.getUserId(), diaryDraftId);

        return ResponseEntity.ok().build();
    }

}
