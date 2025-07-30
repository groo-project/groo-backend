package com.x1.groo.security.service;

import com.x1.groo.security.aggregate.RefreshToken;
import com.x1.groo.security.dto.TokenDTO;
import com.x1.groo.security.jwt.TokenProvider;
import com.x1.groo.security.repository.RefreshTokenRepository;
import com.x1.groo.security.vo.LoginRequestVO;
import com.x1.groo.security.vo.LoginResponseVO;
import com.x1.groo.security.vo.TokenRequestVO;
import com.x1.groo.user.aggregate.UserEntity;
import com.x1.groo.user.repository.UserRepository;
import com.x1.groo.user.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    //    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;


    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       PasswordEncoder passwordEncoder, TokenProvider tokenProvider,
                       RefreshTokenRepository refreshTokenRepository, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
    }

    @Transactional
    public LoginResponseVO signup(LoginRequestVO loginRequestVO) {
        if (userRepository.existsByEmail(loginRequestVO.getEmail())) {
            throw new RuntimeException("이미 가입되어 있는 유저입니다");
        }

        UserEntity user = loginRequestVO.toUserEntity(passwordEncoder);
        return LoginResponseVO.of(userRepository.save(user));
    }

    @Transactional
    public TokenDTO login(LoginRequestVO loginRequestVO) {
        // Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = loginRequestVO.toAuthenticationToken();

        // 검증 (사용자 비밀번호 체크)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 로그인한 유저의 정보 가져오기
        UserEntity userEntity = userRepository.findByEmail(loginRequestVO.getEmail())
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        // 인증 정보를 기반으로 JWT 토큰 생성
        TokenDTO tokenDTO = tokenProvider.generateTokenDTO(
                authentication,
                userEntity.getId(),
                userEntity.getNickname());

        // RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(tokenDTO.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        // 토큰 발급
        return TokenDTO.builder()
                .grantType(tokenDTO.getGrantType())
                .accessToken(tokenDTO.getAccessToken())
                .refreshToken(tokenDTO.getRefreshToken())
                .accessTokenExpiresIn(tokenDTO.getAccessTokenExpiresIn())
//                .userId(userEntity.getId())
                .userNickname(userEntity.getNickname())
                .build();
    }

    @Transactional
    public TokenDTO reissue(TokenRequestVO tokenRequestDto) {
        // Refresh Token 검증
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 로그인한 유저의 정보 가져오기
        UserEntity userEntity = userRepository.findByEmail(tokenRequestDto.getAccessToken())
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        // 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        // Refresh Token 일치하는지 검사
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 새로운 토큰 생성
        TokenDTO tokenDto = tokenProvider.generateTokenDTO(
                authentication,
                userEntity.getId(),
                userEntity.getNickname());

        // 저장소 정보 업데이트
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        // 토큰 발급
        return tokenDto;
    }
}
