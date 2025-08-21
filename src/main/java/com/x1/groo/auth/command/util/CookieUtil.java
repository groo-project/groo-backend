package com.x1.groo.auth.command.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.Duration;
import java.util.Map;

public class CookieUtil {

    private static final boolean PROD = false; // 운영이면 true

    public static void setRefreshCookie(HttpServletResponse res, String token, Duration maxAge) {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .path("/")
                .domain("localhost")
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
                .domain("localhost")
                .maxAge(0)
                .sameSite(PROD ? "None" : "Lax")
                .secure(PROD)
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
