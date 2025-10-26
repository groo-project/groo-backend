package com.x1.groo.common.sse;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class ForestEventService {

    private final SseEmitterRegistry registry;

    // 선택: 대량 전송 시 요청 스레드 점유 방지
    private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void sendEvent(int forestId, String eventName, Object payload) {

        // sendEvent 시작

        var emitters = registry.getAll(forestId);
        if (emitters.isEmpty()) return;

        // 비동기 이벤트 전송
        for (SseEmitter emitter : emitters) {
            executor.execute(() -> safeSend(emitter, eventName, payload));
        }

        System.out.println("=== ForestEventService.sendEvent 완료 ===");

    }

    public void sendHeartbeat(int forestId) {
        sendEvent(forestId, "HEARTBEAT", "pong");
    }

    private void safeSend(SseEmitter emitter, String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(payload));
        } catch (Exception e) {
            try { emitter.complete(); } catch (Exception ignore) {}
        }
    }
}
