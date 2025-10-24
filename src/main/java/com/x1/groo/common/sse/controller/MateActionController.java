package com.x1.groo.common.sse.controller;

import com.x1.groo.common.sse.ForestEventService;
import com.x1.groo.common.sse.payload.UserJoinedPayload;
import com.x1.groo.forest.mate.command.application.service.CommandMateService;
import com.x1.groo.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mate")
public class MateActionController {

    private final ForestEventService forestEventService;
    private final CommandMateService mateCommandService;


    // 수락
    @PostMapping("/{forestId}/join")
    public ResponseEntity<Void> join(@PathVariable int forestId,
                                     @AuthenticationPrincipal CustomUserDetails user) {

//        System.out.println("=== 초대 수락 시작 ===");
//        System.out.println("Forest ID: " + forestId);
//        System.out.println("User ID: " + user.getUserId());
//        System.out.println("User Name: " + user.getName());

        // 1) DB 갱신 (짧은 트랜잭션으로)
        mateCommandService.joinForest(user.getUserId(), forestId, user.getName());
        System.out.println("DB 갱신 완료");


        // 2) 브로드캐스트 (DB와 분리)
        System.out.println("이벤트 전송 시작");
        forestEventService.sendEvent(forestId, "USER_JOINED", new UserJoinedPayload(user.getUserId(), user.getName()));
        System.out.println("이벤트 전송 완료");

        return ResponseEntity.ok().build();
    }


}
