package com.x1.groo.common.exception;

import com.x1.groo.discord.DiscordNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordNotifier discordNotifier;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {

        ErrorCode errorCode = ex.getErrorCode();

        if (ex.getCause() != null) {
            discordNotifier.sendError(
                    "Custom Exception",
                    "에러 코드: " + errorCode.getCode() +
                            "\n메시지: " + errorCode.getMessage() +
                            "\ncause: " + ex.getCause()
            );
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {

        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        discordNotifier.sendError(
                "Unhandled Exception",
                ex.toString()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }
}
