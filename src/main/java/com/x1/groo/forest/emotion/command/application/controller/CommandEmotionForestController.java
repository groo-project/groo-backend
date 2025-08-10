package com.x1.groo.forest.emotion.command.application.controller;

import com.x1.groo.common.JwtUtil;
import com.x1.groo.forest.emotion.command.application.service.CommandEmotionForestService;
import com.x1.groo.forest.emotion.command.domain.vo.RequestCreateVO;
import com.x1.groo.forest.emotion.command.domain.vo.RequestMailboxVO;
import com.x1.groo.forest.emotion.command.domain.vo.RequestPlacementVO;
import com.x1.groo.forest.emotion.command.domain.vo.RequestReplacementVO;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "감정숲", description = "감정의 숲 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/emotion-forest")
@Slf4j
public class CommandEmotionForestController {

    private final CommandEmotionForestService commandEmotionForestService;
    private final JwtUtil jwtUtil;

    @Autowired
    public CommandEmotionForestController(CommandEmotionForestService commandEmotionForestService,
                                          JwtUtil jwtUtil) {
        this.commandEmotionForestService = commandEmotionForestService;
        this.jwtUtil = jwtUtil;
    }


    /**
     * 아이템 회수
     * @param authorizationHeader 토큰
     * @param placementIds 배치된 아이템 배치 id 목록
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "기록의 조각 회수")
    @DeleteMapping("/placement")
    public ResponseEntity<Void> retrieveItemByIds(@RequestHeader(value = "Authorization") String authorizationHeader,
                                                 @RequestParam List<Integer> placementIds) {

        // "Bearer " 부분 제거
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        commandEmotionForestService.retrieveItemByIds(userId, placementIds);

        return ResponseEntity.ok().build();
    }


    /**
     * 아이템 전체 회수
     * @param authorizationHeader 토큰
     * @param forestId 전체 회수를 진행 할 숲 id
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "모든 기록의 조각 회수")
    @DeleteMapping("/placements")
    public ResponseEntity<Void> retrieveAllItems(@RequestHeader(value = "Authorization") String authorizationHeader,
                                                 @RequestParam int forestId) {

        // "Bearer " 부분 제거
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        commandEmotionForestService.retrieveAllItems(userId, forestId);

        return ResponseEntity.ok().build();
    }


    /**
     * 아이템 배치
     * @param authorizationHeader 토큰
     * @param requestPlacementVO 배치할 아이템 정보 요청 객체
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "기록의 조각 배치")
    @PostMapping("/placement")
    public ResponseEntity<Void> placement(@RequestHeader(value = "Authorization") String authorizationHeader,
                                          @RequestBody RequestPlacementVO requestPlacementVO) {

        // "Bearer " 부분 제거
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        commandEmotionForestService.placeItem(userId, requestPlacementVO);

        return ResponseEntity.ok().build();
    }


    /**
     * 배치된 아이템 재배치
     * @param authorizationHeader 토큰
     * @param replacementVOList 재배치할 아이템 정보 요청 객체 목록
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "배치된 기록의 조각 수정")
    @PatchMapping("/placement")
    public ResponseEntity<Void> replacement(@RequestHeader(value = "Authorization") String authorizationHeader,
                                            @RequestBody List<RequestReplacementVO> replacementVOList) {

        // "Bearer " 부분 제거
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        commandEmotionForestService.replaceItem(userId, replacementVOList);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "방명록 등록")
    @PostMapping("/mailbox")
    public ResponseEntity<Void> createMailbox(@RequestHeader(value = "Authorization") String authorizationHeader,
                                              @RequestBody RequestMailboxVO requestMailboxVO) {

        // "Bearer " 부분 제거
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        commandEmotionForestService.createMailbox(userId, requestMailboxVO);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "방명록 삭제")
    @DeleteMapping("/mailbox")
    public ResponseEntity<Void> deleteMailbox(@RequestHeader(value = "Authorization") String authorizationHeader,
                                              @RequestParam int mailboxId,
                                              @RequestParam int forestId) {

        // "Bearer " 부분 제거
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        commandEmotionForestService.deleteMailbox(userId, mailboxId, forestId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "숲 공개여부")
    @PatchMapping("/public/{forestId}")
    public ResponseEntity<Void> updateForestPublic(@PathVariable int forestId,
                                                   @RequestHeader(value = "Authorization") String authorizationHeader) {
        // "Bearer " 부분 제거
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);  // JWT 토큰 파싱
        int userId = ((Number) claims.get("userId")).intValue();  // userId 추출

        // 숲 공개여부 변경 로직 실행
        commandEmotionForestService.updateForestPublic(forestId, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "숲 생성")
    @PostMapping("/new")
    public ResponseEntity<Map<String, String>> createEmotionForest(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody RequestCreateVO request) {

        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        commandEmotionForestService.createEmotionForest(userId, request);

        return ResponseEntity.ok(Map.of("message", "감정의 숲이 생성되었습니다."));
    }

    @Operation(summary = "숲 이름 수정")
    @PatchMapping("/{forestId}/name")
    public ResponseEntity<Void> updateForestName(@PathVariable int forestId,
                                                 @RequestHeader(value = "Authorization") String authorizationHeader,
                                                 @RequestBody Map<String, String> request) {
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        String newName = request.get("name");

        commandEmotionForestService.updateForestName(forestId, userId, newName);

        return ResponseEntity.ok().build();
    }


}
