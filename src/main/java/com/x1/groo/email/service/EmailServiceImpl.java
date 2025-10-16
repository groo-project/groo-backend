package com.x1.groo.email.service;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.email.aggregate.EmailEntity;
import com.x1.groo.email.config.RedisUtil;
import com.x1.groo.email.dto.EmailCheckDTO;
import com.x1.groo.email.repository.EmailRepository;
import com.x1.groo.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;
    private final UserService userService;
    private final EmailRepository emailRepository;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;

    private int authNumber;

    // 임의의 6자리 양수를 반환 (이메일 인증 코드)
    public void makeRandomNumber() {
        Random r = new Random();
        authNumber = r.nextInt(900000) + 100000; // 100000 ~ 999999
    }

    @Transactional
    @Override
    public String joinEmail(String email) {
        makeRandomNumber();
        String setFrom = "\"Groo Admin\" <x1grooservice@gmail.com>";
        String title = "Groo 회원 가입 인증 이메일 입니다.";

        EmailEntity entity = new EmailEntity();
        entity.setEmail(email);
        entity.setVerificationCode(String.valueOf(authNumber));

        try {
            // HTML 템플릿 로드
            String content = loadHtmlTemplate("template/email/signup.html");

            // 템플릿에 인증 번호 삽입
            content = content.replace("${authNumber}", String.valueOf(authNumber));

            // 이메일 발송
            mailSend(setFrom, email, title, content);

            emailRepository.save(entity);

            return Integer.toString(authNumber);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.EMAIL_TEMPLATE_LOAD_FAIL, e);
        }
    }

    // HTML 템플릿 로드 메서드
    private String loadHtmlTemplate(String path) throws IOException {
        // ClassPathResource를 사용하여 리소스 폴더의 템플릿 로드
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }

    // 이메일 전송
    public void mailSend(String setFrom, String toMail, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(setFrom);
            helper.setTo(toMail);
            helper.setSubject(title);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAIL, e);
        }

        // key : 이메일, value : 인증 번호
        redisUtil.setDataExpire(toMail, Integer.toString(authNumber), authCodeExpirationMillis / 60000);
    }

    @Transactional
    @Override
    public String findPassword(String email) {
        makeRandomNumber();
        String setFrom = "\"Groo Admin\" <x1grooservice@gmail.com>";
        String title = "Groo 비밀번호 재설정 인증 이메일 입니다.";

        EmailEntity entity = new EmailEntity();
        entity.setEmail(email);
        entity.setVerificationCode(String.valueOf(authNumber));

        try {
            // HTML 템플릿 로드
            String content = loadHtmlTemplate("template/email/find-password.html");

            // 템플릿에 인증 번호 삽입
            content = content.replace("${authNumber}", String.valueOf(authNumber));

            // 이메일 발송
            mailSend(setFrom, email, title, content);

            emailRepository.save(entity);

            return Integer.toString(authNumber);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.EMAIL_TEMPLATE_LOAD_FAIL, e);
        }

    }

    @Transactional
    public String withdraw(String email) {

        makeRandomNumber();
        String setFrom = "\"Groo Admin\" <x1grooservice@gmail.com>";
        String title = "Groo 회원 탈퇴 인증 이메일 입니다.";

        EmailEntity entity = new EmailEntity();
        entity.setEmail(email);
        entity.setVerificationCode(String.valueOf(authNumber));

        try {
            // HTML 템플릿 로드
            String content = loadHtmlTemplate("template/email/withdraw.html");

            // 템플릿에 인증 번호 삽입
            content = content.replace("${authNumber}", String.valueOf(authNumber));

            // 이메일 발송
            mailSend(setFrom, email, title, content);

            emailRepository.save(entity);

            return Integer.toString(authNumber);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.EMAIL_TEMPLATE_LOAD_FAIL, e);
        }
    }
}
