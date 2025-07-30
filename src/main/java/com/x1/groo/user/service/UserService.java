package com.x1.groo.user.service;

import com.x1.groo.email.dto.EmailCheckDTO;
import com.x1.groo.security.vo.LoginResponseVO;
import com.x1.groo.user.vo.SignupRequestVO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    String registerUser(@Valid SignupRequestVO signupRequestVO);

    LoginResponseVO findMemberInfoById(Long userId);

    LoginResponseVO findMemberInfoByEmail(String email);

    boolean isNicknameExists(String nickname);

    boolean isEmailRegistered(String email);

    ResponseEntity<String> verifyEmailAuthentication(@Valid EmailCheckDTO emailCheckDto);
}
