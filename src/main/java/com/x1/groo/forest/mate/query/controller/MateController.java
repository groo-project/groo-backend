package com.x1.groo.forest.mate.query.controller;

import com.x1.groo.common.JwtUtil;
import com.x1.groo.forest.mate.query.dto.DiaryByDateDTO;
import com.x1.groo.forest.mate.query.dto.DiaryByMonthDTO;
import com.x1.groo.forest.mate.query.dto.MateForestDetailDTO;
import com.x1.groo.forest.mate.query.dto.MateForestResponseDTO;
import com.x1.groo.forest.mate.query.service.MateServiceImpl;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/mate")
@RequiredArgsConstructor
public class MateController {

    private final MateServiceImpl mateService;
    private final JwtUtil jwtUtil;

    /* 날짜별 일기 조회 */
    @GetMapping("/diary/{forestId}/date")
    public ResponseEntity<List<DiaryByDateDTO>> getDiariesByDate(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable int forestId,
            @RequestParam LocalDate date
    ) {
        // JWT에서 userId 추출
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        // 서비스 호출
        List<DiaryByDateDTO> diaries = mateService.findDiaries(userId, forestId, date);
        return ResponseEntity.ok(diaries);
    }

    /* 월별 일기 조회 */
    @GetMapping("/diary/{forestId}/month")
    public ResponseEntity<List<DiaryByMonthDTO>> getDiariesByMonth(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable int forestId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        List<DiaryByMonthDTO> diaries = mateService.findDiariesByMonth(userId, forestId, year, month);
        return ResponseEntity.ok(diaries);
    }

    /* 유저가 입장중인 우정의 숲 조회 */
    @GetMapping("/forests")
    public List<MateForestResponseDTO> getMyForests(
            @RequestHeader(value = "Authorization") String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        // 유저 ID로 우정의 숲 리스트 조회
        return mateService.getForestsByUserId(userId);
    }

    /* 우정의 숲 상세 조회 */
    @GetMapping("/detail/{forestId}")
    public MateForestDetailDTO getForestDetail(@PathVariable int forestId) {

        return mateService.getForestDetail(forestId);
    }
}
