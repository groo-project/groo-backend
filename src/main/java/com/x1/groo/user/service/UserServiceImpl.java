package com.x1.groo.user.service;

import com.x1.groo.email.config.RedisUtil;
import com.x1.groo.email.dto.EmailCheckDTO;
import com.x1.groo.email.exception.CustomException;
import com.x1.groo.forest.emotion.command.application.service.CommandEmotionForestService;
import com.x1.groo.forest.emotion.command.domain.vo.RequestCreateVO;
import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.dto.TokenDTO;
import com.x1.groo.security.vo.LoginRequestVO;
import com.x1.groo.security.vo.LoginResponseVO;
import com.x1.groo.user.aggregate.Role;
import com.x1.groo.user.aggregate.UserEntity;
import com.x1.groo.user.dto.UserDTO;
import com.x1.groo.user.repository.UserRepository;
import com.x1.groo.user.vo.SignupRequestVO;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;
    private final RedisUtil redisUtil;
    private final CommandEmotionForestService forestService;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                           ModelMapper modelMapper, RedisUtil redisUtil, CommandEmotionForestService forestService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.modelMapper = modelMapper;
        this.redisUtil = redisUtil;
        this.forestService = forestService;
    }

    // 기능 : 회원가입
    @Override
    public String registerUser(@Valid SignupRequestVO signupRequestVO) throws CustomException {
        // 이메일 중복 체크
        Optional<UserEntity> existingUser = userRepository.findByEmailOrNickname(signupRequestVO.getEmail(),
                signupRequestVO.getNickname());
        if (existingUser.isPresent()) {
            throw new CustomException("이미 존재하는 닉네임입니다.");
        }

        // 이메일 인증 여부 확인
        if (!redisUtil.exists(signupRequestVO.getEmail())) {
            throw new CustomException("이메일 인증을 먼저 진행해 주세요.");
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
                .orElseThrow(() -> new RuntimeException("로그인 유저 정보가 없습니다."));
    }

    @Override
    public LoginResponseVO findMemberInfoByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(LoginResponseVO::of)
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));
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
            return ResponseEntity.badRequest().body("이미 가입된 이메일입니다.");
        }

        // 인증번호 직접 검증
        String storedAuthNum = redisUtil.getData(emailCheckDto.getEmail());

        if (storedAuthNum == null || !storedAuthNum.equals(emailCheckDto.getAuthNum())) {
            return ResponseEntity.badRequest().body("인증 번호가 일치하지 않습니다.");
        }

        return ResponseEntity.ok("인증 성공");
    }

    // login 할때 자동 호출될 메소드
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return null;
//    }

    /* 설명. spring security 사용 시 프로바이더에서 활요할 로그인용 메소드(id로 회원 조회해서 UserDetails 타입을 반환하는 메소드) */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity loginUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email + " 해당 유저를 찾을 수 없습니다."));  // email 필드로 where절을 걸어서 조회하는 쿼리 메소드

        /* 설명. 사용자가 로그인 시 id를(이메일을) 잘못 입력 했다면 */
//        if(loginUser == null) {
//            throw new UsernameNotFoundException(email + " 이메일 아이디의 유저는 존재하지 않습니다.");
//        }

        // 권한 만들기
        List<GrantedAuthority> auths =
                List.of(new SimpleGrantedAuthority("ROLE_" + loginUser.getRole().name()));

        // DTO → CustomUserDetails
//        UserDTO dto = UserDTO.of(loginUser);
        UserDTO dto = UserDTO.builder()
                .id(loginUser.getId())                 // int/Long 맞춰서
                .email(loginUser.getEmail())
                .password(loginUser.getPassword())     // 외부 응답에 쓰지 않을 거면 @JsonIgnore 권장
                .nickname(loginUser.getNickname())
//                .type(loginUser.getType())             // enum ROLE/TYPE 같은 거 쓰면 그대로
                // .status(e.getStatus())      // 있으면 추가
                .build();

        return new CustomUserDetails(dto, auths);
    }


    @Override
    public UserDTO getUserById(String memNo) {
        UserEntity foundUser = userRepository.findById(Long.parseLong(memNo)).get();

        UserDTO userDTO = modelMapper.map(foundUser, UserDTO.class);

        return userDTO;
    }
}
