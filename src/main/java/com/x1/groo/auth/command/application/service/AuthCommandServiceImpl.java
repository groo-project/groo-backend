package com.x1.groo.auth.command.application.service;

import com.x1.groo.auth.command.application.vo.GoogleLogin;
import com.x1.groo.auth.command.application.aggregate.RefreshToken;
import com.x1.groo.auth.command.application.vo.RefreshResult;
import com.x1.groo.auth.command.domain.repository.RefreshTokenRepository;
import com.x1.groo.auth.command.util.HashUtil;
import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.forest.common.domain.aggregate.BackgroundEntity;
import com.x1.groo.forest.common.domain.aggregate.ForestEntity;
import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import com.x1.groo.forest.common.domain.repository.BackgroundRepository;
import com.x1.groo.forest.common.domain.repository.ForestRepository;
import com.x1.groo.forest.common.domain.repository.UserRepository;
import com.x1.groo.forest.emotion.command.domain.repository.EmotionSharedForestRepository;
import com.x1.groo.forest.mate.command.domain.repository.SharedForestRepository;
import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.user.dto.UserDTO;
import com.x1.groo.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthCommandServiceImpl implements AuthCommandService{

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ForestRepository forestRepository;
    private final BackgroundRepository backgroundRepository;

    public AuthCommandServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                  JwtUtil jwtUtil,
                                  UserService userService,
                                  UserRepository userRepository,
                                  ForestRepository forestRepository,
                                  BackgroundRepository backgroundRepository){
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userRepository = userRepository;
        this.forestRepository = forestRepository;
        this.backgroundRepository = backgroundRepository;
    }

    @Override
    @Transactional
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

        String accessToken = jwtUtil.generateAccessToken(user.getUserId(),user.getName(), user.getNickname(), roles);
        return new RefreshResult(accessToken, newRt, jwtUtil.getRefreshTtl());
    }

    @Transactional
    @Override
    public GoogleLogin loginWithGoogle(String sub, String email, String name) {
        // 1) 사용자 upsert (구글 sub로 조회)
        UserEntity user = userRepository.findByOauthProviderAndOauthId("google",sub)
                .orElseGet(() -> {
                    UserEntity u = new UserEntity();
                    u.setOauthProvider("google");
                    u.setOauthId(sub);
                    u.setEmail(email);
                    u.setNickname(name != null ? name : "user");
                    return userRepository.save(u);
                });
        BackgroundEntity background = backgroundRepository.findById(1)
                .orElseThrow(() -> new CustomException(ErrorCode.BACKGROUND_NOT_FOUND));

        ForestEntity forest = forestRepository.findByUser_Email(email) // <- 네 도메인에 맞게
                .orElseGet(() -> {
                    ForestEntity f = new ForestEntity();
                    f.setUser(user); ;                 // owner 매핑이 있다면
                    f.setBackground(background);      // 연관 매핑일 때
                    f.setIsPublic(false);
                    f.setMonth(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
                    f.setName(user.getNickname());
                    return forestRepository.save(f);
                });

        // 이미 숲이 있다면 배경 업데이트
//        if (forest.getBackground() == null || !forest.getBackground().getId().equals(1)) {
//            forest.setBackground(background);     // 연관 매핑일 때
//            // 또는: forest.setBackgroundId(1);
//            forestRepository.save(forest);        // (영속이면 생략 가능하지만 안전하게 저장)
//        }

        // 2) AT 발급 (네 시그니처에 맞춰 int id, email, nickname, roles)
        String access = jwtUtil.generateAccessToken(
                user.getId(),                             // int
                user.getEmail(),
                user.getNickname(),
                java.util.Collections.singletonList("ROLE_USER")
        );

        return new GoogleLogin(user.getId(), user.getNickname(), access);
    }
}
