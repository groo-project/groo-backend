package com.x1.groo.forest.emotion.command.application.controller;

import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.util.JwtUtil;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "개별 기록의 조각 회수")
    @DeleteMapping("/placement")
    public ResponseEntity<Void> retrieveItemById(@AuthenticationPrincipal CustomUserDetails user,
                                                 @RequestParam int placementId) {

        int userId = user.getUserId();

        commandEmotionForestService.retrieveItemById(userId, placementId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모든 기록의 조각 회수")
    @DeleteMapping("/placements")
    public ResponseEntity<Void> retrieveAllItems(@AuthenticationPrincipal CustomUserDetails user,
                                                 @RequestParam int forestId) {

        int userId = user.getUserId();

        commandEmotionForestService.retrieveAllItems(userId, forestId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "기록의 조각 배치")
    @PostMapping("/placement")
    public ResponseEntity<Void> placement(@AuthenticationPrincipal CustomUserDetails user,
                                          @RequestBody RequestPlacementVO requestPlacementVO) {

        int userId = user.getUserId();

        commandEmotionForestService.placeItem(userId, requestPlacementVO);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "배치된 기록의 조각 수정")
    @PatchMapping("/placement")
    public ResponseEntity<Void> replacement(@AuthenticationPrincipal CustomUserDetails user,
                                            @RequestBody RequestReplacementVO requestReplacementVO) {

        int userId = user.getUserId();

        commandEmotionForestService.replaceItem(userId, requestReplacementVO);

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
