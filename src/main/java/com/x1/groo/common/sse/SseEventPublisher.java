package com.x1.groo.common.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 도메인 이벤트(SSE)를 일관된 포맷/규칙으로 발행하는 고수준 퍼블리셔.
 * - afterCommit 보장(롤백 시 발행 안 함)
 * - forestId별 증가 시퀀스(seq) 부여 → Last-Event-ID 연동
 * - 공통 Envelope 포맷으로 전송
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SseEventPublisher {

    private final SseEmitterRegistry sseEmitterRegistry;

    // 서버 재시작 시 초기화됨
    // forestId별 이벤트 시퀀스
    /** 숲별 증가 시퀀스를 동시성 안전하게 관리 */
    private final ConcurrentHashMap<Integer, AtomicLong> seqs = new ConcurrentHashMap<>();

    private long nextSeq(int forestId) {
        return seqs
                //forestId의 카운터가 없으면 0으로 초기화해서 생성.
                .computeIfAbsent(forestId, k -> new AtomicLong(0)) // 숲별 시퀀스 카운터 준비
                .incrementAndGet(); // 1 증가 후 값 반환
    }

    public <T> void publish(int forestId, SseEventType type, T payload) {
        publishAfterCommit(forestId, type, payload);
    }
    
    /**  트랜잭션 커밋 이후에만 발행 (롤백 시 미발행) */
    public <T> void publishAfterCommit(int forestId, SseEventType type, T payload) {
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishNow(forestId, type, payload);
                }
            });
        } else {
            // 트랜잭션 없으면 바로 발행
            publishNow(forestId, type,payload);
        }
    }

    /**
     * 즉시 발행 (afterCommit이 필요 없을 때만 사용)
     * 시퀀스·타임스탬프·타입을 넣어 SseEnvelope로 감싸고,
     * id = seq로 설정해 Last-Event-ID 복구까지 대비 → SseEmitterRegistry로 브로드캐스트.
     * */
    private <T> void publishNow(int forestId, SseEventType type, T payload) {

        //seq 부여: 위에서 설명한 숲별 증가 시퀀스를 뽑음 → 이벤트 순서/중복 처리에 핵심.
        long seq = nextSeq(forestId);

        SseEnvelope<T> env = new SseEnvelope<> (
                type.name(),        // 이벤트 타입 문자열 (예: USER_JOINED)
                forestId,           // 대상 숲
                seq,                // 증가 시퀀스
                Instant.now(),      // 서버 타임스탬프
                payload             // 실제 데이터(작게! 델타 권장)
        );
        // id=seq 로 지정 → 브라우저 재연결 시 Last-Event-ID로 넘어옴
        sseEmitterRegistry.sendToForest(forestId, type.name(), env, String.valueOf(seq));
         log.debug("SSE published type={} forestId={} seq={}", type, forestId, seq);
    }

    // 숲 삭제 시 시퀀스 정리
    public void resetSequence(int forestId) {
        seqs.remove(forestId);
    }


}
