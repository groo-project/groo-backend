package com.x1.groo.forest.emotion.query.controller;

import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.forest.emotion.query.dto.*;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionDetailDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionListDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionMailboxDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionMailboxListDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionUserItemDTO;
import com.x1.groo.forest.emotion.query.service.QueryForestEmotionService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "감정숲")
@RestController
@RequestMapping("/api")
@Slf4j
public class QueryForestEmotionController {

    private final JwtUtil jwtUtil;
    private final QueryForestEmotionService queryForestEmotionService;

    @Autowired
    public QueryForestEmotionController(JwtUtil jwtUtil, QueryForestEmotionService queryForestEmotionService) {
        this.jwtUtil = jwtUtil;
        this.queryForestEmotionService = queryForestEmotionService;
    }

    @Operation(summary = "기록의 조각 조회")
    @GetMapping("/items/{categoryId}/{forestId}")
    public ResponseEntity<?> getItems(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int categoryId,
            @PathVariable int forestId) {

        int userId = user.getUserId();

        log.info("userId = {}", userId);

        List<QueryForestEmotionUserItemDTO> items = queryForestEmotionService.getPieceOfMemory(userId, categoryId, forestId);

        if (items == null || items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("보유한 기억의 조각이 없습니다. 일기를 써서 더 많은 조각들을 모아봐요🌸");
        }

        return ResponseEntity.ok(items);
    }

    @Operation(summary = "작성된 방명록 리스트 조회")
    @GetMapping("/mailbox-lists/{forestId}")
    public ResponseEntity<List<QueryForestEmotionMailboxListDTO>> getMailboxList(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int forestId) {

        int userId = user.getUserId();

        List<QueryForestEmotionMailboxListDTO> result = queryForestEmotionService.getMailboxList(userId, forestId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "감정의 숲에 작성된 방명록 상세 조회")
    @GetMapping("/mailbox-detail/{id}")
    public ResponseEntity<List<QueryForestEmotionMailboxDTO>> getMailboxDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int id) {

        int userId = user.getUserId();

        List<QueryForestEmotionMailboxDTO> result = queryForestEmotionService.getMailboxDetail(userId, id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "감정의 숲 상세 조회")
    @GetMapping("/detail/{forestId}")
    public ResponseEntity<List<QueryForestEmotionDetailDTO>> getForestDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int forestId) {

        int userId = user.getUserId();

        List<QueryForestEmotionDetailDTO> result = queryForestEmotionService.getForestDetail(userId, forestId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "소유한 숲 조회")
    @GetMapping("/myforest")
    public ResponseEntity<List<QueryForestEmotionListDTO>> getMyForest(
            @AuthenticationPrincipal CustomUserDetails user) {
        log.info("principal userId={}, email={}", user.getUserId(), user.getUsername());

        int userId = user.getUserId();

        List<QueryForestEmotionListDTO> result = queryForestEmotionService.getForestList(userId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "날짜별 일기 조회")
    @GetMapping("/diary/{forestId}/date")
    public ResponseEntity<List<QueryForestEmotionDiaryByDateDTO>> getDiariesByDate(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int forestId,
            @RequestParam LocalDate date
    ) {
        int userId = user.getUserId();

        // 서비스 호출
        List<QueryForestEmotionDiaryByDateDTO> diaries = queryForestEmotionService.findDiaries(userId, forestId, date);
        return ResponseEntity.ok(diaries);
    }

    @Operation(summary = "월별 일기 조회")
    @GetMapping("/diary/{forestId}/month")
    public ResponseEntity<List<QueryForestEmotionDiaryByMonthDTO>> getDiariesByMonth(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int forestId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        int userId = user.getUserId();

        List<QueryForestEmotionDiaryByMonthDTO> diaries = queryForestEmotionService.findDiariesByMonth(userId, forestId, year, month);
        return ResponseEntity.ok(diaries);
    }
}
