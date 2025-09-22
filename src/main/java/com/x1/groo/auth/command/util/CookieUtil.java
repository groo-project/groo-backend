package com.x1.groo.auth.command.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtil {

    private static final boolean PROD = true; // 운영이면 true

    /**  AT (SSE 인증) */
    public static void setAccessCookie(HttpServletResponse res, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .path("/")                       // 모든 API에 전송
                // .domain("localhost")          // X: 로컬에서는 지정하지 말 것 (호스트 전용 쿠키)
                .maxAge(maxAge)
                .sameSite(PROD ? "None" : "Lax")
                .secure(PROD)                    // 운영 HTTPS면 true
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void clearAccessCookie(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite(PROD ? "None" : "Lax")
                .secure(PROD)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void setRefreshCookie(HttpServletResponse res, String token, Duration maxAge) {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .path("/")
//                .domain("localhost")
                .maxAge(maxAge)
                .sameSite(PROD ? "None" : "Lax")
                .secure(PROD)
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void clearRefreshCookie(HttpServletResponse res) {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
//                .domain("localhost")
                .maxAge(0)
                .sameSite(PROD ? "None" : "Lax")
                .secure(PROD)
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
