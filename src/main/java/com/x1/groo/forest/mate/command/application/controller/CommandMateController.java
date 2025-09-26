package com.x1.groo.forest.mate.command.application.controller;

import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.forest.mate.command.application.service.CommandMateService;
import com.x1.groo.forest.mate.command.domain.vo.CreateInviteRequest;
import com.x1.groo.forest.mate.command.domain.vo.CreateMateForestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "우정의 숲", description = "우정의 숲 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/api/mate")
@Slf4j
public class CommandMateController {

    private final CommandMateService commandMateService;

    @Autowired
    public CommandMateController(CommandMateService commandMateService) {
        this.commandMateService = commandMateService;
    }

    @Operation(summary = "공유의 숲 탈퇴 및 숲 삭제", description = "숲의 정원이 0명일 경우 숲이 삭제됩니다.")
    @Transactional
    @DeleteMapping("/quit")
    public ResponseEntity<String> quit(@AuthenticationPrincipal CustomUserDetails user,
                                       @RequestParam int forestId) {

        int userId = user.getUserId();
        commandMateService.quit(userId, forestId);

        return ResponseEntity.ok("공유의 숲 탈퇴 되었습니다.");
    }

    @Operation(summary = "초대 링크 생성")
    @GetMapping("/link")
    public CreateInviteRequest createInviteLink(@RequestParam int forestId,
                                                @AuthenticationPrincipal CustomUserDetails user) {

        int userId = user.getUserId();
        String inviteCode = commandMateService.createInviteLink(forestId, userId);

        String inviteLink = "http://localhost:5173/mate/invite/" + inviteCode;
        return new CreateInviteRequest(inviteLink);
    }

    @Operation(summary = "초대 수락")
    @PostMapping("/accept/{inviteCode}")
    public ResponseEntity<Map<String,Integer>> acceptInvite(@AuthenticationPrincipal CustomUserDetails user,
                                               @PathVariable String inviteCode) {

        int userId = user.getUserId();
        int forestId = commandMateService.acceptInvite(userId, inviteCode);

        return ResponseEntity.ok(Map.of("forestId",forestId));

    }

    @Operation(summary = "우정의 숲 새로 만들기")
    @PostMapping("/forests/new")
    public ResponseEntity<Map<String, String>> createMateForest(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CreateMateForestRequest request) {

        int userId = user.getUserId();

        commandMateService.createMateForest(userId, request);

        return ResponseEntity.ok(Map.of("message", "우정의 숲이 생성되었습니다."));
    }

}
