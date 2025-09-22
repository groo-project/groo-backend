package com.x1.groo.common.exception;

import com.x1.groo.notifier.Notifier;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Notifier notifier;

    public GlobalExceptionHandler(@Qualifier("discordNotifier") Notifier notifier) {
        this.notifier = notifier;
    }

    private boolean isSse(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("text/event-stream");
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex, HttpServletRequest req) {

        ErrorCode errorCode = ex.getErrorCode();
        String cause = ex.getCause() != null ? ex.getCause().toString() : "";

        if (ex.getCause() != null) {
            notifier.sendError(
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

    /**
     * 정적 리소스 못 찾을 때는 알림 X
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest req) {

        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        notifier.sendError(
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
