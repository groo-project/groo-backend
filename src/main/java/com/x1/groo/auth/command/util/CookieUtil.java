package com.x1.groo.auth.command.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtil {
    public static void setRefreshCookie(HttpServletResponse res, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)        // 로컬 개발에서 http라면 필요 시 false로 낮춰 테스트
                .sameSite("None")    // 5173→8080 크로스사이트에서 필수
                .path("/api")        // /api/auth/*에 모두 전송됨
                .maxAge(maxAge)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void clearRefreshCookie(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).sameSite("None").path("/api").maxAge(0).build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
