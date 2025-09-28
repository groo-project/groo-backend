package com.x1.groo.email.controller;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.email.dto.EmailCheckDTO;
import com.x1.groo.email.service.EmailServiceImpl;
import com.x1.groo.email.vo.EmailRequestVO;
import com.x1.groo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이메일", description = "이메일 인증 코드 전송 및 인증 코드 검증 기능을 제공합니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mails")
public class EmailController {
    private final EmailServiceImpl mailService;
    private final UserService userService;

    @Operation(summary = "인증 코드 전송")
    @PostMapping
    public String mailSend(@RequestBody @Valid EmailRequestVO emailRequestVO) {
        if(userService.findByEmail(emailRequestVO.getEmail())){
            throw new CustomException(ErrorCode.USER_EMAIL_DUPLICATE);
        }
        return mailService.joinEmail(emailRequestVO.getEmail());
    }

    @Operation(summary = "인증 코드 일치 여부 확인")
    @PostMapping("/verification")
    public ResponseEntity<String> verifyEmail(@RequestBody @Valid EmailCheckDTO emailCheckDto) {

        return userService.verifyEmailAuthentication(emailCheckDto);
    }
}
