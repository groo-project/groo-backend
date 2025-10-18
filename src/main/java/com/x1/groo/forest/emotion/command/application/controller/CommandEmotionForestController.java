package com.x1.groo.forest.emotion.command.application.controller;

import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.forest.emotion.command.application.service.CommandEmotionForestService;
import com.x1.groo.forest.emotion.command.domain.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "감정숲", description = "감정의 숲 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/api/emotion-forest")
@Slf4j
public class CommandEmotionForestController {

    private final CommandEmotionForestService commandEmotionForestService;

    @Autowired
    public CommandEmotionForestController(CommandEmotionForestService commandEmotionForestService) {
        this.commandEmotionForestService = commandEmotionForestService;
    }

    /**
     * 아이템 회수
     * @param user 회원 정보
     * @param placementIds 배치된 아이템 배치 id 목록
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "기록의 조각 회수")
    @DeleteMapping("/placement")
    public ResponseEntity<Void> retrieveItems(@AuthenticationPrincipal CustomUserDetails user,
                                                 @RequestParam List<Integer> placementIds) {

        int userId = user.getUserId();

        commandEmotionForestService.retrieveItemsByIds(userId, placementIds);

        return ResponseEntity.ok().build();
    }


    /**
     * 아이템 전체 회수
     * @param user 회원 정보
     * @param forestId 전체 회수를 진행 할 숲 id
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "모든 기록의 조각 회수")
    @DeleteMapping("/placements")
    public ResponseEntity<Void> retrieveAllItems(@AuthenticationPrincipal CustomUserDetails user,
                                                 @RequestParam int forestId) {

        int userId = user.getUserId();

        commandEmotionForestService.retrieveAllItems(userId, forestId);

        return ResponseEntity.ok().build();
    }


    /**
     * 아이템 배치
     * @param user 회원 정보
     * @param requestPlacementVO 배치할 아이템 정보 요청 객체
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "기록의 조각 배치")
    @PostMapping("/placement")
    public ResponseEntity<Void> placeItem(@AuthenticationPrincipal CustomUserDetails user,
                                          @RequestBody RequestPlacementVO requestPlacementVO) {

        int userId = user.getUserId();

        commandEmotionForestService.placeItem(userId, requestPlacementVO);

        return ResponseEntity.ok().build();
    }


    /**
     * 배치된 아이템 재배치
     * @param user 회원 정보
     * @param replacementVOList 재배치할 아이템 정보 요청 객체 목록
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "배치된 기록의 조각 수정")
    @PatchMapping("/placement")
    public ResponseEntity<Void> replaceItems(@AuthenticationPrincipal CustomUserDetails user,
                                            @RequestBody List<RequestReplacementVO> replacementVOList) {

        int userId = user.getUserId();

        commandEmotionForestService.replaceItem(userId, replacementVOList);

        return ResponseEntity.ok().build();
    }

    /**
     * 보관된 아이템 배치
     * @param user 회원 정보
     * @param requestReplantVO 배치 및 보관된 아이템 정보
     * @return 정상 처리 시 200 반환
     */
    @Operation(summary = "보관된 기록의 조각 배치")
    @PostMapping("/placements/from-storage")
    public ResponseEntity<Void> placeStoredItem(@AuthenticationPrincipal CustomUserDetails user,
                                        @RequestBody RequestReplantVO requestReplantVO) {

        int userId = user.getUserId();

        commandEmotionForestService.placeStoredItem(userId, requestReplantVO);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "방명록 등록")
    @PostMapping("/mailbox")
    public ResponseEntity<Void> createMailbox(@AuthenticationPrincipal CustomUserDetails user,
                                              @RequestBody RequestMailboxVO requestMailboxVO) {

        int userId = user.getUserId();

        commandEmotionForestService.createMailbox(userId, requestMailboxVO);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "방명록 삭제")
    @DeleteMapping("/mailbox")
    public ResponseEntity<Void> deleteMailbox(@AuthenticationPrincipal CustomUserDetails user,
                                              @RequestParam int mailboxId,
                                              @RequestParam int forestId) {

        int userId = user.getUserId();

        commandEmotionForestService.deleteMailbox(userId, mailboxId, forestId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "숲 공개여부")
    @PatchMapping("/public/{forestId}")
    public ResponseEntity<Void> updateForestPublic(@PathVariable int forestId,
                                                   @AuthenticationPrincipal CustomUserDetails user) {

        int userId = user.getUserId();

        // 숲 공개여부 변경 로직 실행
        commandEmotionForestService.updateForestPublic(forestId, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "숲 생성")
    @PostMapping("/new")
    public ResponseEntity<Map<String, String>> createEmotionForest(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody RequestCreateVO request) {

        int userId = user.getUserId();

        commandEmotionForestService.createEmotionForest(userId, request);

        return ResponseEntity.ok(Map.of("message", "감정의 숲이 생성되었습니다."));
    }

    @Operation(summary = "숲 이름 수정")
    @PatchMapping("/{forestId}/name")
    public ResponseEntity<Void> updateForestName(@PathVariable int forestId,
                                                 @AuthenticationPrincipal CustomUserDetails user,
                                                 @RequestBody Map<String, String> request) {

        int userId = user.getUserId();

        String newName = request.get("name");

        commandEmotionForestService.updateForestName(forestId, userId, newName);

        return ResponseEntity.ok().build();
    }


}
