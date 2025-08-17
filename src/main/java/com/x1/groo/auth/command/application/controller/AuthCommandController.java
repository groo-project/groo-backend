package com.x1.groo.auth.command.application.controller;

import com.x1.groo.auth.command.application.dto.RefreshDTO;
import com.x1.groo.auth.command.application.vo.RefreshResult;
import com.x1.groo.auth.command.application.service.AuthCommandService;
import com.x1.groo.auth.command.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
        if( rt == null || rt.isBlank()) {
            return ResponseEntity.status(401).body("NO_REFRESH_TOKEN");
        }

        try {
            // 서비스는 RT를 검증하고, 회전(rotate)해서 새 RT/AT를 반환
            RefreshResult result = authCommandService.refresh(rt);

            // 새 RT를 HttpOnly 쿠키로 재설정 (SameSite=None; Path는 /api 또는 /api/auth)
            CookieUtil.setRefreshCookie(res, result.getNewRefreshToken(), result.getRefreshTtl());

            // 프론트가 기대하는 응답(AT 필수, user/roles는 선택)
            return ResponseEntity.ok(new RefreshDTO(
                    result.getAccessToken()
//                    result.getUser(),          // 필요 없으면 null
//                    result.getRoles()          // 필요 없으면 Collections.emptyList()
            ));
        } catch (BadCredentialsException e) {
            // 재사용/만료/위조 등 → 쿠키 제거 후 401
            CookieUtil.clearRefreshCookie(res);
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

    }

}
