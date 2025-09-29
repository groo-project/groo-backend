package com.x1.groo.user.service;

import com.x1.groo.email.dto.EmailCheckDTO;
import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.security.dto.TokenDTO;
import com.x1.groo.security.vo.LoginRequestVO;
import com.x1.groo.security.vo.LoginResponseVO;
import com.x1.groo.user.dto.LoginDTO;
import com.x1.groo.user.dto.UserDTO;
import com.x1.groo.user.vo.FindPasswordRequestVO;
import com.x1.groo.user.vo.SignupRequestVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    String registerUser(@Valid SignupRequestVO signupRequestVO);

    LoginResponseVO findMemberInfoById(Integer userId);

    LoginResponseVO findMemberInfoByEmail(String email);

    boolean isNicknameExists(String nickname);

    ResponseEntity<String> verifyEmailAuthentication(@Valid EmailCheckDTO emailCheckDto);

    UserDTO getUserById(String memNo);

    UserDetails loadUserByUsername(String email);

    LoginDTO login(LoginRequestVO loginRequestVO);

    void updateNickname(int userId, String nickname);

    boolean findByEmail(@Email @NotEmpty(message = "이메일을 입력해 주세요") String email);

    void findPassword(FindPasswordRequestVO findPasswordRequestVO);
}
