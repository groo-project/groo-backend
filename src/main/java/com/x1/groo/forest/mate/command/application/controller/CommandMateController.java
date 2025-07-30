package com.x1.groo.forest.mate.command.application.controller;

import com.x1.groo.common.JwtUtil;
import com.x1.groo.forest.mate.command.application.service.CommandMateService;
import com.x1.groo.forest.mate.command.domain.vo.CreateInviteRequest;
import com.x1.groo.forest.mate.command.domain.vo.CreateMateForestRequest;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mate")
@Slf4j
public class CommandMateController {

    private final JwtUtil jwtUtil;
    private final CommandMateService commandMateService;

    @Autowired
    public CommandMateController(JwtUtil jwtUtil, CommandMateService commandMateService) {
        this.jwtUtil = jwtUtil;
        this.commandMateService = commandMateService;
    }

    @Transactional
    // 공유의 숲 탈퇴 및 숲 삭제(0명이 되었을 때)
    @DeleteMapping("/quit")
    public ResponseEntity<String> quit(@RequestHeader(value = "Authorization") String authorizationHeader,
                                       @RequestParam int forestId) {
        String token = authorizationHeader.replace("Bearer ", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        commandMateService.quit(userId, forestId);
        return ResponseEntity.ok("공유의 숲 탈퇴 되었습니다.");
    }

    // 초대 링크 생성
    @GetMapping("/link")
    public CreateInviteRequest createInviteLink(@RequestParam int forestId) {

        // 초대 링크 생성하고 결과 받기
        String inviteCode = commandMateService.createInviteLink(forestId);

        // 초대코드를 이용해서 최종 초대링크로 감싸기
        String inviteLink = "http://localhost:5173/mate/invite/" + inviteCode;
        return new CreateInviteRequest(inviteLink);
    }

    // 초대 수락
    @PostMapping("/accept/{inviteCode}")
    public ResponseEntity<String> acceptInvite(@RequestHeader(value = "Authorization") String authorizationHeader,
                                               @PathVariable String inviteCode) {
        // "Bearer " 부분 제거
        String token = authorizationHeader.replace("Bearer ", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        commandMateService.acceptInvite(userId, inviteCode);

        return ResponseEntity.ok("초대 수락 성공");

    }

    /* 우정의 숲 새로 만들기 */
    @PostMapping("/forests/new")
    public ResponseEntity<Map<String, String>> createMateForest(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody CreateMateForestRequest request) {

        String token = authorizationHeader.replace("Bearer", "").trim();
        Claims claims = jwtUtil.parseJwt(token);
        int userId = ((Number) claims.get("userId")).intValue();

        commandMateService.createMateForest(userId, request);

        return ResponseEntity.ok(Map.of("message", "우정의 숲이 생성되었습니다."));
    }

}
