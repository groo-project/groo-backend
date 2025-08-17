package com.x1.groo.auth.command.application.service;

import com.x1.groo.auth.command.application.aggregate.RefreshToken;
import com.x1.groo.auth.command.application.vo.RefreshResult;
import com.x1.groo.auth.command.domain.repository.RefreshTokenRepository;
import com.x1.groo.auth.command.util.HashUtil;
import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.user.dto.UserDTO;
import com.x1.groo.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthCommandServiceImpl implements AuthCommandService{

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthCommandServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                  JwtUtil jwtUtil,
                                  UserService userService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Operation(
            summary = "accessToken 재발급",
            description = "accessToken 만료 시 refreshToken을 이용해서 accessToken 재발급"
    )
    @Override
    public RefreshResult refresh(String rt) {
        // 1) RT 파싱/검증
        Jws<Claims> jws = jwtUtil.parserClaimsJws(rt);
        String typ = jws.getBody().get("typ", String.class);
        if (!"RT".equals(typ)) {
            throw new BadCredentialsException("NOT_REFRESH_TOKEN");
        }

        int userId = jwtUtil.getUserId(jws);
        String subjectEmail = jws.getBody().getSubject();
        String jti = jws.getBody().getId();

        // 2) DB 에서 현재 RT(jti_hash) 찾기
        java.util.Optional<RefreshToken> opt = refreshTokenRepository.findByJtiHash((HashUtil.sha256(jti)));
        if (opt.isEmpty()) {
            throw new BadCredentialsException("RT_NOT_FOUND_OR_REUSED");
        }
        RefreshToken current = opt.get();
        if (current.isRevoked() || current.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new BadCredentialsException("RT_EXPIRED_OR_REVOKED");
        }

        // 3) 회전: 기존 RT 삭제 → 새 RT 발급/저장
        refreshTokenRepository.delete(current);

        String newRt = jwtUtil.generateRefreshToken(userId);
        Jws<Claims> newRtJws = jwtUtil.parserClaimsJws(newRt);
        String newJti = newRtJws.getBody().getId();

        RefreshToken next = new RefreshToken();
        next.setUserId(userId);
        next.setJtiHash(HashUtil.sha256(newJti));
        next.setExpiresAt(java.time.LocalDateTime.now().plus(jwtUtil.getRefreshTtl()));
        refreshTokenRepository.save(next);

        // 4) 새 AT 발급
        UserDTO userDTO = userService.getUserById(String.valueOf(userId));
        CustomUserDetails user = new CustomUserDetails(userDTO);

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()); // JDK 8~11 (16+면 .toList())
        // dto -> userDetails
//        CustomUserDetails user = users.toUserDetails(users);
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(),user.getName(), roles);
        return new RefreshResult(accessToken, newRt, jwtUtil.getRefreshTtl());
    }
}
