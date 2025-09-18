package com.x1.groo.auth.command.application.controller;

import com.x1.groo.auth.command.application.vo.RefreshResult;
import com.x1.groo.auth.command.application.service.AuthCommandService;
import com.x1.groo.auth.command.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthCommandController {

    private final AuthCommandService authCommandService;

    public AuthCommandController(AuthCommandService authCommandService) {
        this.authCommandService = authCommandService;
    }

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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse res) {

        CookieUtil.clearRefreshCookie(res);
        CookieUtil.clearAccessCookie(res);

        return ResponseEntity.ok().build();
    }


}
