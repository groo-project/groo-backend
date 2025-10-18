package com.x1.groo.user.service;

import com.x1.groo.auth.command.application.aggregate.RefreshToken;
import com.x1.groo.auth.command.domain.repository.RefreshTokenRepository;
import com.x1.groo.auth.command.util.HashUtil;
import com.x1.groo.email.aggregate.EmailEntity;
import com.x1.groo.email.config.RedisUtil;
import com.x1.groo.email.dto.EmailCheckDTO;
import com.x1.groo.email.repository.EmailRepository;
import com.x1.groo.forest.common.domain.repository.ForestRepository;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.forest.emotion.command.application.service.CommandEmotionForestService;
import com.x1.groo.forest.emotion.command.domain.vo.RequestCreateVO;
import com.x1.groo.forest.mate.command.domain.repository.ForestInviteRepository;
import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.security.vo.LoginRequestVO;
import com.x1.groo.security.vo.LoginResponseVO;
import com.x1.groo.user.aggregate.Role;
import com.x1.groo.user.aggregate.UserEntity;
import com.x1.groo.user.dto.KakaoUserInfoDTO;
import com.x1.groo.user.dto.LoginDTO;
import com.x1.groo.user.dto.LoginUserDTO;
import com.x1.groo.user.dto.UserDTO;
import com.x1.groo.user.repository.UserRepository;
import com.x1.groo.user.vo.FindPasswordRequestVO;
import com.x1.groo.user.vo.ResetPasswordRequestVO;
import com.x1.groo.user.vo.SignupRequestVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Builder
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String OAUTH_PROVIDER_KAKAO = "KAKAO";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;
    private final RedisUtil redisUtil;
    private final CommandEmotionForestService forestService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ForestRepository forestRepository;
    private final EmailRepository emailRepository;
    private final ForestInviteRepository forestInviteRepository;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                           ModelMapper modelMapper, RedisUtil redisUtil, CommandEmotionForestService forestService,
                           JwtUtil jwtUtil,
                           RefreshTokenRepository refreshTokenRepository,
                           ForestRepository forestRepository, EmailRepository emailRepository, ForestInviteRepository forestInviteRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.modelMapper = modelMapper;
        this.redisUtil = redisUtil;
        this.forestService = forestService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.forestRepository = forestRepository;
        this.emailRepository = emailRepository;
        this.forestInviteRepository = forestInviteRepository;
    }

    @Override
    public boolean findByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // 기능 : 회원가입
    @Transactional
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
    public LoginResponseVO findMemberInfoById(Integer userId) {
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

    @Transactional
    @Override
    public ResponseEntity<String> verifyEmailAuthentication(EmailCheckDTO emailCheckDto) {

        String email = emailCheckDto.getEmail();

        EmailEntity entity = emailRepository.findFirstByEmailOrderByCreatedAtDesc(email);

        String storedAuthNum = entity.getVerificationCode();
        if (storedAuthNum == null || !storedAuthNum.equals(emailCheckDto.getAuthNum())) {
            throw new CustomException(ErrorCode.USER_EMAIL_AUTH_FAILED);
        }


        entity.setVerified(true);
        entity.setUsedAt(LocalDateTime.now());

        emailRepository.save(entity);

        return ResponseEntity.ok("인증 성공");
    }

    @Transactional
    @Override
    public LoginDTO login(LoginRequestVO req) {

        //  사용자 조회
        UserEntity loginUser = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        //  비밀번호 검증
        if (!bCryptPasswordEncoder.matches(req.getPassword(), loginUser.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }


        //  UserDTO로 변환 → CustomUserDetails 생성
        UserDTO userDto = UserDTO.fromEntity(loginUser);
        CustomUserDetails user = new CustomUserDetails(userDto);


        //  권한 문자열 목록 뽑기 ("ROLE_USER" 형태)
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()); // JDK 8~11 (16+면 .toList())


        //  AT 발급
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getNickname(), user.getNickname(), roles);

        //  RT 발급 & 저장
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

        return LoginDTO.builder()
                .accessToken(accessToken)
                .roles(roles)
                .refreshToken(newRt)
                .user(new LoginUserDTO(user.getUserId(), user.getUsername(), user.getName()))
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

    @Transactional(readOnly = true)
    @Override
    public UserDTO getUserById(String memNo) {
        UserEntity foundUser = userRepository.findById(Integer.parseInt(memNo))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return modelMapper.map(foundUser, UserDTO.class);
    }

    // 비밀번호 찾기
    @Transactional
    @Override
    public void findPassword(FindPasswordRequestVO findPasswordRequestVO) {

        UserEntity user = userRepository.findByEmail(findPasswordRequestVO.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이메일 인증 여부 확인
        if (!redisUtil.exists(findPasswordRequestVO.getEmail())) {
            throw new CustomException(ErrorCode.USER_EMAIL_NOT_VERIFIED);
        }

        // 새로운 비밀번호 작성 후 비밀번호 db에 저장
        user.setPassword(bCryptPasswordEncoder.encode(findPasswordRequestVO.getPassword()));
        userRepository.save(user);

        redisUtil.deleteData(findPasswordRequestVO.getEmail());

    }

    @Transactional
    @Override
    public void resetPassword(int userId, ResetPasswordRequestVO vo) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(bCryptPasswordEncoder.encode(vo.getPassword()));


    }

    @Transactional
    @Override
    public void updateNickname(int userId, String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        UserEntity foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        foundUser.setNickname(nickname);
    }

    @Transactional
    public LoginDTO loginOrRegisterKakaoUser(KakaoUserInfoDTO userInfo) {
        UserEntity user = userRepository.findByOauthProviderAndOauthId(OAUTH_PROVIDER_KAKAO, userInfo.getKakaoId().toString())
                .orElseGet(() -> {
                    String nickname = generateUniqueNickname(userInfo.getNickname());

                    UserEntity newUser = new UserEntity();
                    newUser.setNickname(nickname);
                    newUser.setOauthProvider(OAUTH_PROVIDER_KAKAO);
                    newUser.setOauthId(userInfo.getKakaoId().toString());

                    UserEntity savedUser = userRepository.save(newUser);

                    // 숲 자동 생성
                    String forestName = savedUser.getNickname() + "의 숲";
                    RequestCreateVO forestReq = new RequestCreateVO(forestName);
                    forestService.createEmotionForest(savedUser.getId(), forestReq);

                    return savedUser;
                });

        List<String> roles = List.of(user.getRole().toString());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getNickname(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Jws<Claims> jws = jwtUtil.parserClaimsJws(refreshToken);
        int userId = jwtUtil.getUserId(jws);
        String newJti = jws.getBody().getId();

        RefreshToken next = new RefreshToken();
        next.setUserId(userId);
        next.setJtiHash(HashUtil.sha256(newJti));
        next.setExpiresAt(java.time.LocalDateTime.now().plus(jwtUtil.getRefreshTtl()));
        refreshTokenRepository.deleteAllByUserId(userId);
        refreshTokenRepository.save(next);

        return LoginDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .roles(roles)
                .user(new LoginUserDTO(user.getId(), user.getEmail(), user.getNickname()))
                .build();
    }

    private String generateUniqueNickname(String baseNickname) {
        List<String> existingNicknames = userRepository.findNicknamesByBase(baseNickname);

        int maxSuffix = existingNicknames.stream()
                .map(name -> name.replace(baseNickname, ""))
                .filter(suffix -> suffix.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        return maxSuffix == 0 && !existingNicknames.contains(baseNickname)
                ? baseNickname
                : baseNickname + (maxSuffix + 1);
    }
}
