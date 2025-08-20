package com.x1.groo.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String code;
    private final String message;
    private final int status;
    private final String timestamp;

    private ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.status = errorCode.getStatus().value();
        this.message = errorCode.getMessage();
        this.timestamp = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }
}
