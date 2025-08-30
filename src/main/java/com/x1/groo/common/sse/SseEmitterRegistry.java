package com.x1.groo.common.sse;

import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterRegistry {

    // forestId → emitters
    private final ConcurrentMap<Integer, CopyOnWriteArrayList<SseEmitter>> byForest = new ConcurrentHashMap<>();

    // 구독 연결
    public SseEmitter add(int forestId, long timeoutMillis) {
        SseEmitter em = new SseEmitter(timeoutMillis);
        byForest.computeIfAbsent(forestId, k -> new CopyOnWriteArrayList<>()).add(em);

        // 끊기면 자동 제거
        em.onTimeout(() -> remove(forestId, em));
        em.onError(e -> remove(forestId, em));
        em.onCompletion(() -> remove(forestId, em));
        return em;
    }

    // 구독 해제
    public void remove(int forestId, SseEmitter em) {
        var list = byForest.get(forestId);
        if (list != null) {
            list.remove(em);
            if (list.isEmpty()) byForest.remove(forestId);
        }
    }

    /** 변경사항 푸시 (죽은 emitter는 정리) */
    public void sendToForest(int forestId, String eventName, Object payload, @Nullable String id) {
        System.out.println("=== SseEmitterRegistry.sendToForest 시작 ===");
        System.out.println("Forest ID: " + forestId);
        System.out.println("Event Type: " + eventName);
        System.out.println("Payload: " + payload);

        var list = byForest.getOrDefault(forestId, new CopyOnWriteArrayList<>());
        for (SseEmitter em : list) {
            try {
                SseEmitter.SseEventBuilder evt = SseEmitter.event()
                        .name(eventName)
                        .data(payload);
                if (id != null) evt.id(id);
                em.send(evt);
            } catch (IOException e) {
                remove(forestId, em);
            }
        }

        System.out.println("=== SseEmitterRegistry.sendToForest 완료 ===");

    }

    /** 연결 유지용 heartbeat */
    public void pingAll() {
        byForest.forEach((forestId, list) -> {
            for (SseEmitter em : list) {
                try { em.send(SseEmitter.event().name("ping").data("💓")); }
                catch (IOException ignored) { remove(forestId, em); }
            }
        });
    }

    // forestId별 emitter 조회
    public List<SseEmitter> getAll(int forestId) {
        return byForest.getOrDefault(forestId, new CopyOnWriteArrayList<>());

    }
}
