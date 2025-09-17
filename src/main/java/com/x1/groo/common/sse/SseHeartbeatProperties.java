package com.x1.groo.common.sse;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sse.heartbeat")
@Getter
@Setter
public class SseHeartbeatProperties {

    private long periodMs;
    private long idleMs;

}
