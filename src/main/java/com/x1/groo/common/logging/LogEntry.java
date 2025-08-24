package com.x1.groo.common.logging;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LogEntry {

    private LocalDateTime timestamp;
    private String clientIp;
    private String method;
    private String uri;
    private String userAgent;

    @Override
    public String toString() {
        return "LogEntry{" +
                "timestamp=" + timestamp +
                ", clientIp='" + clientIp + '\'' +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
