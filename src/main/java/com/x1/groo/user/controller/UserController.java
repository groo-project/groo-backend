package com.x1.groo.user.controller;

import com.x1.groo.security.dto.TokenDTO;
import com.x1.groo.security.vo.LoginRequestVO;
import com.x1.groo.user.dto.UserDTO;
import com.x1.groo.user.service.UserService;
import com.x1.groo.user.vo.ResponsefindUserVO;
import com.x1.groo.user.vo.SignupRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저", description = "회원가입 및 로그인 기능을 제공합니다.")
@Slf4j
@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "회원 가입")
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody @Valid SignupRequestVO signupRequestVO) {
        return ResponseEntity.ok(userService.registerUser(signupRequestVO));
    }

//    @Operation(summary = "로그인")
//    @PostMapping("/login")
//    public ResponseEntity<TokenDTO> login(@RequestBody LoginRequestVO loginRequestVO) {
//        return ResponseEntity.ok(userService.login(loginRequestVO));
//    }



    @Operation(summary = "회원 조회")
    @GetMapping("{memNO}")
    public ResponseEntity<ResponsefindUserVO> getUsers(@PathVariable String memNo) {
        UserDTO userDTO = userService.getUserById(memNo);

        ResponsefindUserVO findUserVO = modelMapper.map(userDTO, ResponsefindUserVO.class);

        return ResponseEntity.status(HttpStatus.OK).body(findUserVO);
    }
}
