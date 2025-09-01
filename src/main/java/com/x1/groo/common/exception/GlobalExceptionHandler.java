package com.x1.groo.common.exception;

import com.x1.groo.discord.DiscordNotifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordNotifier discordNotifier;

    private boolean isSse(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("text/event-stream");
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex, HttpServletRequest req) {

        ErrorCode errorCode = ex.getErrorCode();
        String cause = ex.getCause() != null ? ex.getCause().toString() : "";

        if (ex.getCause() != null) {
            discordNotifier.sendError(
                    "Custom Exception",
                    "에러 코드: " + errorCode.getCode() +
                            "\n메시지: " + errorCode.getMessage() +
                            "\ncause: " + cause
            );
        }

        // ★ SSE면 바디 금지 (이미 text/event-stream 커밋 이슈 방지)
        if (isSse(req)) {
            return ResponseEntity.status(errorCode.getStatus()).build();
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest req) {

        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        discordNotifier.sendError(
                "Unhandled Exception",
                ex.toString()
        );

        // ★ SSE면 바디 금지
        if (isSse(req)) {
            return ResponseEntity.status(errorCode.getStatus()).build();
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }
}
