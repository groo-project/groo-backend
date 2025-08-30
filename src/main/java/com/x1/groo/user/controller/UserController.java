package com.x1.groo.user.controller;

import com.x1.groo.auth.command.util.CookieUtil;
import com.x1.groo.security.util.JwtUtil;
import com.x1.groo.security.vo.LoginRequestVO;
import com.x1.groo.user.dto.LoginDTO;
import com.x1.groo.user.dto.LoginUserDTO;
import com.x1.groo.user.dto.UserDTO;
import com.x1.groo.user.service.UserService;
import com.x1.groo.user.vo.ResponsefindUserVO;
import com.x1.groo.user.vo.SignupRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "유저", description = "회원가입 및 로그인 기능을 제공합니다.")
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService,
                          ModelMapper modelMapper,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "회원 가입")
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody @Valid SignupRequestVO signupRequestVO) {
        return ResponseEntity.ok(userService.registerUser(signupRequestVO));
    }

    @Operation(summary = "로그인")
    @PostMapping(value = "/login",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginDTO> login(@RequestBody LoginRequestVO loginRequestVO,
                                          HttpServletResponse res) {

        LoginDTO login = userService.login(loginRequestVO);

        LoginUserDTO user = login.getUser();

        String accessToken = jwtUtil.generateAccessToken(user.getUserId(),user.getEmail(),login.getRoles());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        CookieUtil.setRefreshCookie(res, refreshToken, jwtUtil.getRefreshTtl());
        CookieUtil.setAccessCookie(res, accessToken, jwtUtil.getAccessTtl());

        List<String> roles = login.getRoles();

        LoginDTO response = LoginDTO.builder()
                .user(user)
                .accessToken(accessToken)
                .roles(roles)
                .build();

        return ResponseEntity.ok(response);
    }



    @Operation(summary = "회원 조회")
    @GetMapping("{memNo}")
    public ResponseEntity<ResponsefindUserVO> getUsers(@PathVariable String memNo) {
        UserDTO userDTO = userService.getUserById(memNo);

        ResponsefindUserVO findUserVO = modelMapper.map(userDTO, ResponsefindUserVO.class);

        return ResponseEntity.status(HttpStatus.OK).body(findUserVO);
    }
}
