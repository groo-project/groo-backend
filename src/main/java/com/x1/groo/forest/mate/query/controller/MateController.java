package com.x1.groo.forest.mate.query.controller;

import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.forest.mate.query.dto.DiaryByDateDTO;
import com.x1.groo.forest.mate.query.dto.DiaryByMonthDTO;
import com.x1.groo.forest.mate.query.dto.MateForestDetailDTO;
import com.x1.groo.forest.mate.query.dto.MateForestResponseDTO;
import com.x1.groo.forest.mate.query.service.MateServiceImpl;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "우정의 숲")
@RestController
@RequestMapping("/api/mate")
@RequiredArgsConstructor
public class MateController {

    private final MateServiceImpl mateService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "날짜별 일기 조회")
    @GetMapping("/diary/{forestId}/date")
    public ResponseEntity<List<DiaryByDateDTO>> getDiariesByDate(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int forestId,
            @RequestParam LocalDate date
    ) {
        int userId = user.getUserId();
        // 서비스 호출
        List<DiaryByDateDTO> diaries = mateService.findDiaries(userId, forestId, date);
        return ResponseEntity.ok(diaries);
    }

    @Operation(summary = "월별 일기 조회")
    @GetMapping("/diary/{forestId}/month")
    public ResponseEntity<List<DiaryByMonthDTO>> getDiariesByMonth(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int forestId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        int userId = user.getUserId();

        List<DiaryByMonthDTO> diaries = mateService.findDiariesByMonth(userId, forestId, year, month);
        return ResponseEntity.ok(diaries);
    }

    @Operation(summary = "유저가 입장중인 우정의 숲 조회")
    @GetMapping("/forests")
    public List<MateForestResponseDTO> getMyForests(
            @AuthenticationPrincipal CustomUserDetails user) {

        int userId = user.getUserId();

        // 유저 ID로 우정의 숲 리스트 조회
        return mateService.getForestsByUserId(userId);
    }

    @Operation(summary = "우정의 숲 상세 조회")
    @GetMapping("/detail/{forestId}")
    public MateForestDetailDTO getForestDetail(@PathVariable int forestId) {

        return mateService.getForestDetail(forestId);
    }
}
