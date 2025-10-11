package com.x1.groo.common.sse.controller;

import com.x1.groo.common.sse.SseEmitterRegistry;
import com.x1.groo.common.sse.service.MateSseService;
import com.x1.groo.forest.mate.command.domain.repository.SharedForestRepository;
import com.x1.groo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;


@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class MateSseController {

    private final SseEmitterRegistry registry;
    private final MateSseService mateSseService;

    @Operation(summary = "SSE 방식 클라이언트 동기화 | 우정의 숲 이벤트 발생")
    @GetMapping( "/mate/{forestId}/events")
//            , produces = MediaType.TEXT_EVENT_STREAM_VALUE) // 브라우저가 SSE로 인식 (브라우저가 스트림으로 파싱하도록 명시)
    public ResponseEntity<SseEmitter> subscribe(@PathVariable("forestId") int forestId,
                                                @RequestHeader(value = "Last-Event-ID", required = false) String lastId,
                                                @AuthenticationPrincipal CustomUserDetails user) {


        System.out.println("=== SSE 연결 요청 시작 ===");
        System.out.println("forestId: " + forestId);
        System.out.println("principal.getUserId(): " + user.getUserId());

         // 접근 권한 체크
        boolean allowed = mateSseService.checkForestAccess(user.getUserId(), forestId);
        System.out.println("권한 체크 결과: " + allowed);

        if (!allowed) {
            System.out.println("권한 없음 - 403 반환");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 바디 없이 403
        }

        // 성공인 경우에만 SSE 시작 + Content-Type 설정
        SseEmitter em = registry.add(forestId, 10 * 60_000L);
        System.out.println("SseEmitter 생성 완료");

        em.onCompletion(() -> {
            System.out.println("SSE 연결 완료 - 정리");
            registry.remove(forestId, em);
            em.complete();
        });
        em.onTimeout(() -> {
            System.out.println("SSE 연결 타임아웃 - 정리");
            registry.remove(forestId, em);
        });

        // 최초 헬로 핑 (선택)
        try { em.send(SseEmitter.event().name("HELLO").data("connected"));
            System.out.println("HELLO 이벤트 전송 성공");
        } catch (IOException e) {
            System.out.println("HELLO 이벤트 전송 실패: " + e.getMessage());

        }

        // lastId 필요시 여기서 리플레이 정책 구현 가능
        System.out.println("SSE 연결 완료 - 응답 반환");
        return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(em);
    }
}
