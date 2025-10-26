package com.x1.groo.common.sse;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.scheduling.annotation.Scheduled;
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

    // 각 emitter의 마지막 전송 시각(ms)
    private final ConcurrentMap<SseEmitter, AtomicLong> lastSendAt = new ConcurrentHashMap<>();

    private final SseHeartbeatProperties sseHeartbeatProperties;

    public SseEmitterRegistry(SseHeartbeatProperties sseHeartbeatProperties) {
        this.sseHeartbeatProperties = sseHeartbeatProperties;
    }

    // 구독 연결
    public SseEmitter add(int forestId, long timeoutMillis) {
        SseEmitter em = new SseEmitter(timeoutMillis);
        byForest.computeIfAbsent(forestId, k -> new CopyOnWriteArrayList<>()).add(em);
        lastSendAt.put(em, new AtomicLong(System.currentTimeMillis()));

        // 즉시 한 바이트라도 흘려보내 프록시/브라우저에 스트림 확정
        try {
            em.send(SseEmitter.event().comment("open"));
            touch(em);
        } catch (IOException ignored) {
            remove(forestId, em);
        }

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
        lastSendAt.remove(em);

    }

    /** 변경사항 푸시 (죽은 emitter는 정리) */
    public void sendToForest(int forestId, String eventName, Object payload, @Nullable String id) {

        // sseEmitterRegistry.sendToForest 시작

        var list = byForest.getOrDefault(forestId, new CopyOnWriteArrayList<>());
        for (SseEmitter em : list) {
            try {
                SseEmitter.SseEventBuilder evt = SseEmitter.event()
                        .name(eventName)
                        .data(payload);
                if (id != null) evt.id(id);
                em.send(evt);
                touch(em); // 전송시각 갱신
            } catch (IOException e) {
                remove(forestId, em);
            }
        }

    }

    /** 연결 유지용 heartbeat: '유휴'인 연결에만 아주 가벼운 코멘트 핑 */
    @Scheduled(fixedRateString = "${sse.heartbeat.period-ms:15000}")
    public void pingIdleOnly() {
        long now = System.currentTimeMillis();
        byForest.forEach((forestId, list) -> {
            for (SseEmitter em : list) {
                long last = lastSendAt.getOrDefault(em, new AtomicLong(0)).get();
                if (now - last >= sseHeartbeatProperties.getIdleMs()) {
                    try {
                        em.send(SseEmitter.event().comment("keep"));
                        touch(em);
                    } catch (IOException ignored) {
                        remove(forestId, em);
                    }
                }
            }
        });
    }
    private void touch(SseEmitter em) {
        lastSendAt.computeIfAbsent(em, k -> new AtomicLong()).set(System.currentTimeMillis());
    }

    // forestId별 emitter 조회
    public List<SseEmitter> getAll(int forestId) {
        return byForest.getOrDefault(forestId, new CopyOnWriteArrayList<>());

    }

    // 서버 내려갈 때 모든 연결을 먼저 닫기
    public void completeAll(String reason) {
        byForest.forEach((forestId, list) -> {
            // 복사본으로 안전 반복
            for (SseEmitter em : new ArrayList<>(list)) {
                try {
                    // 종료 알림 한 번 날리고
                    try {
                        em.send(SseEmitter.event()
                                .name("server_shutdown")
                                .data(reason == null ? "server shutting down" : reason));
                    } catch (IOException ignore) {
                        // 보내다 실패해도 그냥 닫기
                    }
                    // 깔끔 종료
                    em.complete();
                } catch (Exception ignored) {
                } finally {
                    remove(forestId, em);
                }
            }
        });
        byForest.clear();
    }
}
