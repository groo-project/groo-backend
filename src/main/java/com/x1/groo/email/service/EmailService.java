package com.x1.groo.email.service;

import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    String joinEmail(String email);

    boolean CheckAuthNum(String email, String authNum);

    boolean isEmailRegistered(String email);
}
