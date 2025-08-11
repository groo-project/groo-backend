package com.x1.groo.forest.emotion.command.application.controller;

import com.x1.groo.common.JwtUtil;
import com.x1.groo.forest.emotion.command.application.service.CommandEmotionForestService;
import com.x1.groo.forest.emotion.command.domain.vo.*;
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

    // 토큰에서 userId 추출하는 메서드
    private int extractUserId(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        return ((Number) claims.get("userId")).intValue();
    }

    /**
     * 아이템 회수
     * @param authorizationHeader 토큰
     * @param placementIds 배치된 아이템 배치 id 목록
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "기록의 조각 회수")
    @DeleteMapping("/placement")
    public ResponseEntity<Void> retrieveItems(@RequestHeader(value = "Authorization") String authorizationHeader,
                                                 @RequestParam List<Integer> placementIds) {

        int userId = extractUserId(authorizationHeader);

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

        int userId = extractUserId(authorizationHeader);

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
    public ResponseEntity<Void> placeItem(@RequestHeader(value = "Authorization") String authorizationHeader,
                                          @RequestBody RequestPlacementVO requestPlacementVO) {

        int userId = extractUserId(authorizationHeader);

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
    public ResponseEntity<Void> replaceItems(@RequestHeader(value = "Authorization") String authorizationHeader,
                                            @RequestBody List<RequestReplacementVO> replacementVOList) {

        int userId = extractUserId(authorizationHeader);

        commandEmotionForestService.replaceItem(userId, replacementVOList);

        return ResponseEntity.ok().build();
    }

    /**
     * 보관된 아이템 배치
     * @param authorizationHeader 토큰
     * @param requestReplantVO 배치 및 보관된 아이템 정보
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "보관된 기록의 조각 배치")
    @PostMapping("/placements/from-storage")
    public ResponseEntity<Void> placeStoredItem(@RequestHeader(value = "Authorization") String authorizationHeader,
                                        @RequestBody RequestReplantVO requestReplantVO) {

        int userId = extractUserId(authorizationHeader);

        commandEmotionForestService.placeStoredItem(userId, requestReplantVO);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "방명록 등록")
    @PostMapping("/mailbox")
    public ResponseEntity<Void> createMailbox(@RequestHeader(value = "Authorization") String authorizationHeader,
                                              @RequestBody RequestMailboxVO requestMailboxVO) {

        int userId = extractUserId(authorizationHeader);

        commandEmotionForestService.createMailbox(userId, requestMailboxVO);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "방명록 삭제")
    @DeleteMapping("/mailbox")
    public ResponseEntity<Void> deleteMailbox(@RequestHeader(value = "Authorization") String authorizationHeader,
                                              @RequestParam int mailboxId,
                                              @RequestParam int forestId) {

        int userId = extractUserId(authorizationHeader);

        commandEmotionForestService.deleteMailbox(userId, mailboxId, forestId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "숲 공개여부")
    @PatchMapping("/public/{forestId}")
    public ResponseEntity<Void> updateForestPublic(@PathVariable int forestId,
                                                   @RequestHeader(value = "Authorization") String authorizationHeader) {

        int userId = extractUserId(authorizationHeader);

        // 숲 공개여부 변경 로직 실행
        commandEmotionForestService.updateForestPublic(forestId, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "숲 생성")
    @PostMapping("/new")
    public ResponseEntity<Map<String, String>> createEmotionForest(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody RequestCreateVO request) {

        int userId = extractUserId(authorizationHeader);

        commandEmotionForestService.createEmotionForest(userId, request);

        return ResponseEntity.ok(Map.of("message", "감정의 숲이 생성되었습니다."));
    }

    @Operation(summary = "숲 이름 수정")
    @PatchMapping("/{forestId}/name")
    public ResponseEntity<Void> updateForestName(@PathVariable int forestId,
                                                 @RequestHeader(value = "Authorization") String authorizationHeader,
                                                 @RequestBody Map<String, String> request) {

        int userId = extractUserId(authorizationHeader);

        String newName = request.get("name");

        commandEmotionForestService.updateForestName(forestId, userId, newName);

        return ResponseEntity.ok().build();
    }


}
