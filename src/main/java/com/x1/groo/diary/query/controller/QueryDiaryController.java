package com.x1.groo.diary.query.controller;

import com.x1.groo.diary.query.dto.ResponseDiaryDetailDTO;
import com.x1.groo.diary.query.dto.ResponsePersonalDiaryListDTO;
import com.x1.groo.diary.query.dto.ResponseSharedDiaryListDTO;
import com.x1.groo.diary.query.service.QueryDiaryService;
import com.x1.groo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "일기")
@RestController
@RequestMapping("/api/diaries")
public class QueryDiaryController {

    private final QueryDiaryService queryDiaryService;

    @Autowired
    public QueryDiaryController(QueryDiaryService queryDiaryService) {
        this.queryDiaryService = queryDiaryService;
    }

    @Operation(summary = "개인 월별 일기 목록 조회")
    @GetMapping("/personal")
    public ResponseEntity<List<ResponsePersonalDiaryListDTO>> getDiariesByYearMonth(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam int forestId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<ResponsePersonalDiaryListDTO> diaries =
                queryDiaryService.getDiariesByYearMonth(user.getUserId(), forestId, year, month);

        return ResponseEntity.ok(diaries);
    }

    @Operation(summary = "개인 일기 상세 조회")
    @GetMapping("/personal/detail")
    public ResponseEntity<ResponseDiaryDetailDTO> getDiaryDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam int diaryId
    ) {
        ResponseDiaryDetailDTO diary = queryDiaryService.getDiaryDetail(user.getUserId(), diaryId);

        return ResponseEntity.ok(diary);
    }

    @Operation(summary = "우정 월별 일기 목록 조회")
    @GetMapping("/shared")
    public ResponseEntity<List<ResponseSharedDiaryListDTO>> getSharedDiariesByYearMonth(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam int forestId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<ResponseSharedDiaryListDTO> diaries =
                queryDiaryService.getSharedDiariesByYearMonth(user.getUserId(), forestId, year, month);

        return ResponseEntity.ok(diaries);
    }

    @Operation(summary = "우정 일기 상세 조회")
    @GetMapping("/shared/detail")
    public ResponseEntity<List<ResponseDiaryDetailDTO>> getSharedDiariesDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam List<Integer> diaryIds
    ) {
        List<ResponseDiaryDetailDTO> diaries = queryDiaryService.getSharedDiariesDetail(user.getUserId(), diaryIds);

        return ResponseEntity.ok(diaries);
    }
}
