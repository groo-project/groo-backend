package com.x1.groo.auth.command.application.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.x1.groo.auth.command.application.vo.RefreshResult;
import com.x1.groo.auth.command.application.service.AuthCommandService;
import com.x1.groo.auth.command.util.CookieUtil;
import com.x1.groo.security.JwtAuthenticationProvider;
import com.x1.groo.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthCommandController {

    private final AuthCommandService authCommandService;
    private final JwtUtil jwtUtil;

    @Value("${app.google.client-id}")
    private String googleClientId;


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
    public ResponseEntity<?> google(@RequestBody Map<String,String> body) {
        String idTokenString = body.get("idToken");

//        var verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
//                .setAudience(Collections.singletonList(googleClientId))  // yml 매핑
//                .build();

        if (idTokenString == null || idTokenString.isBlank()) {
            return ResponseEntity.badRequest().body("idToken is required");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                .Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            //  parse() 대신 verify(String) 사용 (파싱+검증)
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google ID token");
            }

            var payload = idToken.getPayload();
            String sub   = payload.getSubject();
            String email = (String) payload.get("email");
            String name  = (String) payload.get("name");

            // 사용자 upsert + AT/RT 발급 (네 서비스 메서드 사용)
            var user = authCommandService.loginWithGoogle(sub, email, name);

            return ResponseEntity.ok(Map.of(
                    "accessToken", user.getAccessToken(),
                    "email",       email,
                    "nickname",    user.getNickname()
            ));
        } catch (GeneralSecurityException | IOException e) {
            log.error("Google verify failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Auth error");
        }
    }



}
