package com.x1.groo.common.sse;

import java.time.Instant;


/** 클라이언트가 공통 포맷으로 파싱하기 위한 래퍼 */
public record SseEnvelope<T>(
        String type,     // "FOREST_UPDATED" / "ITEM_PLACED" / ...
        int forestId,    // 대상 숲
        long seq,        // 증가 시퀀스 (Last-Event-ID 대응)
        Instant ts,      // 서버 생성 시각
        T payload        // 작고 필요한 데이터(델타 권장)
) { }
