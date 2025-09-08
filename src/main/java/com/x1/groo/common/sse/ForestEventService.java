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

        System.out.println("=== ForestEventService.sendEvent 시작 ===");
        System.out.println("Forest ID: " + forestId);
        System.out.println("Event Type: " + eventName);
        System.out.println("Payload: " + payload);

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
            // IOException, IllegalStateException 등: 끊긴 연결이니 정리
            // forestId를 알 수 없으므로 registry에서 일괄 제거 지원이 어렵다?
            // => 보통 emitter->forestId 역맵을 두거나, remove 시 forestId를 함께 전달하도록 호출부에서 처리
            // 여기서는 간단히 complete로 종료 유도
            try { emitter.complete(); } catch (Exception ignore) {}
        }
    }
}
