package com.x1.groo.auth.command.application.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.x1.groo.auth.command.application.vo.GoogleLoginRequestVO;
import com.x1.groo.auth.command.application.vo.RefreshResult;
import com.x1.groo.auth.command.application.service.AuthCommandService;
import com.x1.groo.auth.command.util.CookieUtil;
import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.user.dto.LoginDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthCommandController {

    private final AuthCommandService authCommandService;
    private final JwtUtil jwtUtil;




    @Operation( summary = "AT/RT 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletResponse res,
                                     @CookieValue(value = "refreshToken", required = false) String rt) {

        try {
            RefreshResult result = authCommandService.refresh(rt);

            CookieUtil.setRefreshCookie( res, result.getNewRefreshToken(), result.getRefreshTtl());

            return ResponseEntity.ok(Map.of("accessToken", result.getAccessToken()));

        } catch (BadCredentialsException e) {
            // 재사용/만료/위조 등 → 쿠키 제거 후 401
            CookieUtil.clearRefreshCookie(res);
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

    }

    @Operation( summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse res) {

        CookieUtil.clearRefreshCookie(res);
        CookieUtil.clearAccessCookie(res);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/google")
    public ResponseEntity<?> google(@RequestBody GoogleLoginRequestVO vo,
                                    HttpServletResponse res) {

        try {

            // 사용자 upsert + AT/RT 발급
            LoginDTO login = authCommandService.loginOrRegisterGoogleUser(vo.getIdToken());

            String accessToken = login.getAccessToken();
            String refreshToken = login.getRefreshToken();

            CookieUtil.setRefreshCookie(res, refreshToken, jwtUtil.getRefreshTtl());
            CookieUtil.setAccessCookie(res, accessToken, jwtUtil.getAccessTtl());

            return ResponseEntity.ok(Map.of("accessToken", accessToken));

        } catch (GeneralSecurityException | IOException e) {
            log.error("Google verify failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Auth error");
        }
    }



}
