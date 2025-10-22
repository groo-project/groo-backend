package com.x1.groo.auth.command.application.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.x1.groo.auth.command.application.aggregate.RefreshToken;
import com.x1.groo.auth.command.application.vo.RefreshResultVO;
import com.x1.groo.auth.command.domain.repository.RefreshTokenRepository;
import com.x1.groo.auth.command.util.HashUtil;
import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.diary.command.domain.repository.DiaryRepository;
import com.x1.groo.email.config.RedisUtil;
import com.x1.groo.forest.common.domain.aggregate.BackgroundEntity;
import com.x1.groo.forest.common.domain.aggregate.ForestEntity;
import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import com.x1.groo.forest.common.domain.repository.BackgroundRepository;
import com.x1.groo.forest.common.domain.repository.ForestItemRepository;
import com.x1.groo.forest.common.domain.repository.ForestRepository;
import com.x1.groo.forest.common.domain.repository.UserRepository;
import com.x1.groo.forest.emotion.command.domain.repository.MailboxRepository;
import com.x1.groo.forest.mate.command.domain.aggregate.SharedForestEntity;
import com.x1.groo.forest.mate.command.domain.repository.SharedForestRepository;
import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.user.dto.LoginDTO;
import com.x1.groo.user.dto.LoginUserDTO;
import com.x1.groo.user.dto.UserDTO;
import com.x1.groo.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final DiaryRepository diaryRepository;
    private final MailboxRepository mailboxRepository;
    private final ForestItemRepository forestItemRepository;
    private final RedisUtil redisUtil;
    private final SharedForestRepository sharedForestRepository;

    @Value("${google.client-id}")
    private String googleClientId;

    public AuthCommandServiceImpl(RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil,
                                  UserService userService,
                                  UserRepository userRepository, ForestRepository forestRepository,
                                  BackgroundRepository backgroundRepository, DiaryRepository diaryRepository,
                                  MailboxRepository mailboxRepository, ForestItemRepository forestItemRepository,
                                  RedisUtil redisUtil, SharedForestRepository sharedForestRepository){
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userRepository = userRepository;
        this.forestRepository = forestRepository;
        this.backgroundRepository = backgroundRepository;
        this.diaryRepository = diaryRepository;
        this.mailboxRepository = mailboxRepository;
        this.forestItemRepository = forestItemRepository;
        this.redisUtil = redisUtil;
        this.sharedForestRepository = sharedForestRepository;
    }

    @Override
    @Transactional
    public RefreshResultVO refresh(String rt) {

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

        String accessToken = jwtUtil.generateAccessToken(user.getUserId(),user.getUsername(), user.getNickname(), roles);
        return new RefreshResultVO(accessToken, newRt, jwtUtil.getRefreshTtl());
    }

    @Transactional
    @Override
    public LoginDTO loginOrRegisterGoogleUser(String idTokenString) throws GeneralSecurityException, IOException {


        if (idTokenString == null || idTokenString.isBlank()) {
            throw new CustomException(ErrorCode.IDTOKEN_NOT_FOUND);
        }

        // 파싱+서명/만료 검증
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                .Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        // 토큰 검증
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new CustomException(ErrorCode.INVALID_IDTOKEN);
        }


//        var payload = idToken.getPayload();
        GoogleIdToken.Payload payload = idToken.getPayload();

        String sub   = payload.getSubject();
        String email = (String) payload.get("email");
        String name  = (String) payload.get("name");


        UserEntity user = userRepository.findByOauthProviderAndOauthId("google", sub)
                .orElseGet(() ->
                        userRepository.findByEmail(email)
                                .map(u -> { // 기존 로컬 계정 → 구글 정보 연동
                                    if (u.getOauthProvider() == null || !"google".equals(u.getOauthProvider())) {
                                        u.setOauthProvider("google");
                                        u.setOauthId(sub);
                                        return userRepository.save(u);
                                    }
                                    return u;
                                })
                                .orElseGet(() -> { // 완전 신규
                                    UserEntity u = new UserEntity();
                                    u.setOauthProvider("google");
                                    u.setOauthId(sub);
                                    u.setEmail(email);
//                                    u.setNickname(name != null ? name : "user");
                                    u.setNickname(generateUniqueNickname(name));
                                    return userRepository.save(u);
                                })
                );

        BackgroundEntity background = backgroundRepository.findById(1)
                .orElseThrow(() -> new CustomException(ErrorCode.BACKGROUND_NOT_FOUND));


        ForestEntity forest = forestRepository.findFirstByUser_IdOrderByIdAsc(user.getId())
                .orElseGet(() -> {
                    ForestEntity f = new ForestEntity();
                    f.setUser(user);
                    f.setBackground(background);
                    f.setIsPublic(false);
                    f.setCreatedAt(LocalDateTime.now());
                    f.setName(user.getNickname());
                    return forestRepository.save(f);
                });

        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                List.of(user.getRole().toString())
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken newRt = new RefreshToken();
        newRt.setUserId(user.getId());
        newRt.setJtiHash(HashUtil.sha256(refreshToken));
        newRt.setExpiresAt(java.time.LocalDateTime.now().plus(jwtUtil.getRefreshTtl()));

        refreshTokenRepository.deleteAllByUserId(user.getId());
        refreshTokenRepository.save(newRt);

        return LoginDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(new LoginUserDTO(user.getId(), user.getEmail(), user.getNickname()))
                .roles(List.of(user.getRole().toString()))
                .build();
    }

    private String generateUniqueNickname(String baseNickname) {
        List<UserEntity> existingNickname = userRepository.findAllByNicknameStartingWith(baseNickname);

        int maxSuffix = existingNickname.stream()
                .map(u -> u.getNickname().replace("baseNickname", ""))
                .filter(suffix -> suffix.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        return maxSuffix == 0 && existingNickname.contains(baseNickname)
                ? baseNickname : baseNickname + (maxSuffix + 1);


    }

    @Transactional
    @Override
    public void withdraw(CustomUserDetails user) {

        int userId = user.getUserId();

        UserEntity entity = userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이메일 인증
        if (!redisUtil.exists(user.getUsername())) {
            throw new CustomException(ErrorCode.USER_EMAIL_NOT_VERIFIED);
        }


//        int forestId = user.getForestId();
//        log.info("forest : {}", forestId);

        // 숲 조회
        List<ForestEntity> ownedForests = forestRepository.findAllByUserId(userId);

        for (ForestEntity forest : ownedForests) {

            int forestId = forest.getId();

            // 숲 회원 조회
            List<SharedForestEntity> member = sharedForestRepository.findAllByForestIdOrderByIdAsc(forestId);

            // 숲의 주인인 경우
            if (forest.getUser().getId() == userId) {

                // 숲 주인을 제외한 멤버 필터링
                List<SharedForestEntity> remaining = member.stream()
                        .filter(m -> m.getUserId() != userId)
                        .toList();

                if (!remaining.isEmpty()) {
                    SharedForestEntity newOwnerMember = remaining.get(0);
                    UserEntity newOwner = userRepository.findById(newOwnerMember.getUserId())
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                    forest.setUser(newOwner);
                    forestRepository.save(forest);

                } else { // 멤버가 없다면
                    forestRepository.deleteById(forestId);
                }

            } else { // 개인 숲인 경우
                forestRepository.deleteById(forestId);
            }
        }

        // 참여한 우정의 숲 삭제
        sharedForestRepository.deleteByUserId(userId);

        userRepository.delete(entity);
    }
}
