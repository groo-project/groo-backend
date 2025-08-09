package com.x1.groo.user.controller;

import com.x1.groo.security.dto.TokenDTO;
import com.x1.groo.security.service.AuthService;
import com.x1.groo.security.vo.LoginRequestVO;
import com.x1.groo.user.service.UserService;
import com.x1.groo.user.vo.SignupRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저", description = "회원가입 및 로그인 기능을 제공합니다.")
@Slf4j
@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @Operation(summary = "회원 가입")
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody @Valid SignupRequestVO signupRequestVO) {
        return ResponseEntity.ok(userService.registerUser(signupRequestVO));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody LoginRequestVO loginRequestVO) {
        return ResponseEntity.ok(authService.login(loginRequestVO));
    }
}
