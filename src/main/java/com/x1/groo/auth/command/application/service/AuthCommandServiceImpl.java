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
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.sql.Ref;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
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

        Jws<Claims> jws = jwtUtil.parserClaimsJws(rt);
        String typ = jws.getBody().get("typ", String.class);
        if (!"RT".equals(typ)) {
            throw new BadCredentialsException("NOT_REFRESH_TOKEN");
        }

        int userId = jwtUtil.getUserId(jws);
        String jti = jws.getBody().getId();

        int subjectUserId = Integer.parseInt(jws.getBody().getSubject());
        if (subjectUserId != userId) {
            throw new BadCredentialsException("User ID mismatch in JWT subject");
        }

        java.util.Optional<RefreshToken> opt = refreshTokenRepository.findByUserId(subjectUserId);

        if (opt.isEmpty()) {
            log.error("RT_NOT_FOUND_OR_REUSED: JTI hash {} not found in database", HashUtil.sha256(jti));
            throw new BadCredentialsException("RT_NOT_FOUND_OR_REUSED");
        }


        RefreshToken current = opt.get();
        if (current.isRevoked() || current.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new BadCredentialsException("RT_EXPIRED_OR_REVOKED");
        }
        // 회전: 기존 RT 삭제 → 새 RT 발급/저장
        refreshTokenRepository.delete(current);

        String newRt = jwtUtil.generateRefreshToken(userId);
        Jws<Claims> newRtJws = jwtUtil.parserClaimsJws(newRt);
        String newJti = newRtJws.getBody().getId();

        RefreshToken next = new RefreshToken();
        next.setUserId(userId);
        next.setJtiHash(HashUtil.sha256(newJti));
        next.setExpiresAt(java.time.LocalDateTime.now().plus(jwtUtil.getRefreshTtl()));
        refreshTokenRepository.save(next);

        UserDTO userDTO = userService.getUserById(String.valueOf(userId));
        CustomUserDetails user = new CustomUserDetails(userDTO);

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String accessToken = jwtUtil.generateAccessToken(user.getUserId(),user.getName(), roles);
        return new RefreshResult(accessToken, newRt, jwtUtil.getRefreshTtl());
    }
}
