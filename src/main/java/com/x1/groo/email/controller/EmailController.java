package com.x1.groo.email.controller;

import com.x1.groo.email.dto.EmailCheckDTO;
import com.x1.groo.email.service.EmailServiceImpl;
import com.x1.groo.email.vo.EmailRequestVO;
import com.x1.groo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mails")
public class EmailController {
    private final EmailServiceImpl mailService;
    private final UserService userService;

    // 인증 코드 전송
    @PostMapping
    public String mailSend(@RequestBody @Valid EmailRequestVO emailRequestVO) {
        return mailService.joinEmail(emailRequestVO.getEmail());
    }

    // 인증 코드 일치 여부 확인
    @PostMapping("/verification")
    public ResponseEntity<String> verifyEmail(@RequestBody @Valid EmailCheckDTO emailCheckDto) {
        return userService.verifyEmailAuthentication(emailCheckDto);
    }
}
