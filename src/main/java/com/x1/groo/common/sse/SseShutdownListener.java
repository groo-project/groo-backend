package com.x1.groo.common.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseShutdownListener {

    private final SseEmitterRegistry registry;

    @EventListener(ContextClosedEvent.class)
    public void onShutDown() {
        registry.completeAll("server is shutting down");
    }
}
