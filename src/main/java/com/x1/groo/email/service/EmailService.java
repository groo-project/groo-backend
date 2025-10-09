package com.x1.groo.email.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    String joinEmail(String email);

    String findPassword(@Email @NotEmpty(message = "이메일을 입력해 주세요") String email);
}
