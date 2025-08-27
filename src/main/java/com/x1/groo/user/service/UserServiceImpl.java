package com.x1.groo.user.service;

import com.x1.groo.auth.command.application.aggregate.RefreshToken;
import com.x1.groo.auth.command.domain.repository.RefreshTokenRepository;
import com.x1.groo.auth.command.util.HashUtil;
import com.x1.groo.email.config.RedisUtil;
import com.x1.groo.email.dto.EmailCheckDTO;
import com.x1.groo.email.exception.CustomException;
import com.x1.groo.forest.common.domain.repository.ForestRepository;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.forest.emotion.command.application.service.CommandEmotionForestService;
import com.x1.groo.forest.emotion.command.domain.vo.RequestCreateVO;
import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.security.vo.LoginRequestVO;
import com.x1.groo.security.vo.LoginResponseVO;
import com.x1.groo.user.aggregate.Role;
import com.x1.groo.user.aggregate.UserEntity;
import com.x1.groo.user.dto.LoginDTO;
import com.x1.groo.user.dto.LoginUserDTO;
import com.x1.groo.user.dto.UserDTO;
import com.x1.groo.user.repository.UserRepository;
import com.x1.groo.user.vo.SignupRequestVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Collection;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Builder
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;
    private final RedisUtil redisUtil;
    private final CommandEmotionForestService forestService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ForestRepository forestRepository;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                           ModelMapper modelMapper, RedisUtil redisUtil, CommandEmotionForestService forestService,
                           JwtUtil jwtUtil,
                           RefreshTokenRepository refreshTokenRepository,
                           ForestRepository forestRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.modelMapper = modelMapper;
        this.redisUtil = redisUtil;
        this.forestService = forestService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.forestRepository = forestRepository;
    }

    // 기능 : 회원가입
    @Override
    public String registerUser(@Valid SignupRequestVO signupRequestVO) throws CustomException {
        // 이메일 중복 체크
        Optional<UserEntity> existingUser = userRepository.findByEmailOrNickname(signupRequestVO.getEmail(),
                signupRequestVO.getNickname());
        if (existingUser.isPresent()) {
            throw new CustomException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        // 이메일 인증 여부 확인
        if (!redisUtil.exists(signupRequestVO.getEmail())) {
            throw new CustomException(ErrorCode.USER_EMAIL_NOT_VERIFIED);
        }

        // DTO → Entity 변환 / 엔티티의 password 컬럼에 암호화 된 값을 추가
        UserEntity newUser = modelMapper.map(signupRequestVO, UserEntity.class);
        newUser.setPassword(bCryptPasswordEncoder.encode(signupRequestVO.getPassword())); // 비밀번호 암호화
        newUser.setRole(Role.COMMON);

        userRepository.save(newUser);
        redisUtil.deleteData(signupRequestVO.getEmail());

        // 숲 자동 생성
        String forestName = newUser.getNickname() + "의 숲";
        RequestCreateVO forestReq = new RequestCreateVO(forestName);
        forestService.createEmotionForest(newUser.getId(), forestReq);

        return "회원가입이 완료되었습니다.";
    }

    @Override
    public LoginResponseVO findMemberInfoById(Long userId) {
        return userRepository.findById(userId)
                .map(LoginResponseVO::of)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public LoginResponseVO findMemberInfoByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(LoginResponseVO::of)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public boolean isNicknameExists(String nickname) {
        return userRepository.existsByEmail(nickname);
    }

    @Override
    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public ResponseEntity<String> verifyEmailAuthentication(EmailCheckDTO emailCheckDto) {
        // 이메일 중복 확인
        if (isEmailRegistered(emailCheckDto.getEmail())) {
            throw new CustomException(ErrorCode.USER_EMAIL_DUPLICATE);
        }

        // 인증번호 직접 검증
        String storedAuthNum = redisUtil.getData(emailCheckDto.getEmail());

        if (storedAuthNum == null || !storedAuthNum.equals(emailCheckDto.getAuthNum())) {
            throw new CustomException(ErrorCode.USER_EMAIL_AUTH_FAILED);
        }

        return ResponseEntity.ok("인증 성공");
    }

    @Transactional
    @Override
    public LoginDTO login(LoginRequestVO req) {

        // 1) 사용자 조회
        UserEntity loginUser = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 2) 비밀번호 검증
        if (!bCryptPasswordEncoder.matches(req.getPassword(), loginUser.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }


        // 3) UserDTO로 변환 → CustomUserDetails 생성
        UserDTO userDto = UserDTO.fromEntity(loginUser);   // ⚠️ fromEntity 에서 id 타입 주의(아래 참고)
        CustomUserDetails user = new CustomUserDetails(userDto);


        // 4) 권한 문자열 목록 뽑기 ("ROLE_USER" 형태)
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()); // JDK 8~11 (16+면 .toList())


        // 4) AT 발급 (짧은 만료, 예: 15분)
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(),user.getName(), roles);

        // 5) RT 발급 & 저장(회전) (긴 만료, 예: 14일)
        String newRt = jwtUtil.generateRefreshToken(user.getUserId());

        Jws<Claims> jws = jwtUtil.parserClaimsJws(newRt);
        int userId = jwtUtil.getUserId(jws);
        String newJti = jws.getBody().getId();

        RefreshToken next = new RefreshToken();
        next.setUserId(userId);
        next.setJtiHash(HashUtil.sha256(newJti));
        next.setExpiresAt(java.time.LocalDateTime.now().plus(jwtUtil.getRefreshTtl()));
        refreshTokenRepository.deleteAllByUserId(userId);
        refreshTokenRepository.save(next);

        int forestId = forestRepository.findActiveForestIdByUserId(loginUser.getId());

        return LoginDTO.builder()
                .accessToken(accessToken)
                .roles(roles)
                .refreshToken(newRt)
                .user(new LoginUserDTO(user.getUserId(), user.getUsername(), user.getName(), forestId))
                .build();
    }

    /* 설명. spring security 사용 시 프로바이더에서 활요할 로그인용 메소드(id로 회원 조회해서 UserDetails 타입을 반환하는 메소드) */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity loginUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));  // email 필드로 where절을 걸어서 조회하는 쿼리 메소드

        // DTO → CustomUserDetails
        UserDTO dto = UserDTO.builder()
                .id(loginUser.getId())
                .email(loginUser.getEmail())
                .password(loginUser.getPassword())
                .nickname(loginUser.getNickname())
                .build();

        return new CustomUserDetails(dto);
    }


    @Override
    public UserDTO getUserById(String memNo) {
        UserEntity foundUser = userRepository.findById(Long.parseLong(memNo)).get();

        UserDTO userDTO = modelMapper.map(foundUser, UserDTO.class);

        return userDTO;
    }
}
