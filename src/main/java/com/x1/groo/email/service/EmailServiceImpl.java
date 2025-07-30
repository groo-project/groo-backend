package com.x1.groo.email.service;

import com.x1.groo.email.config.RedisUtil;
import com.x1.groo.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;
    private final UserService userService;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender, RedisUtil redisUtil, UserService userService) {
        this.mailSender = mailSender;
        this.redisUtil = redisUtil;
        this.userService = userService;
    }

    private int authNumber;

    // 임의의 6자리 양수를 반환 (이메일 인증 코드)
    public void makeRandomNumber() {
        Random r = new Random();
        authNumber = r.nextInt(900000) + 100000; // 100000 ~ 999999
    }

    @Override
    public String joinEmail(String email) {
        makeRandomNumber();
        String setFrom = "\"Groo Admin\" <x1grooservice@gmail.com>";
        String toMail = email;
        String title = "Groo 회원 가입 인증 이메일 입니다.";

        try {
            // HTML 템플릿 로드
            String content = loadHtmlTemplate();

            // 템플릿에 인증 번호 삽입
            content = content.replace("${authNumber}", String.valueOf(authNumber));

            // 이메일 발송
            mailSend(setFrom, toMail, title, content);

            return Integer.toString(authNumber);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // HTML 템플릿 로드 메서드
    private String loadHtmlTemplate() throws IOException {
        // ClassPathResource를 사용하여 리소스 폴더의 템플릿 로드
        ClassPathResource resource = new ClassPathResource("email-template.html");
        byte[] fileBytes = Files.readAllBytes(Paths.get(resource.getURI()));
        return new String(fileBytes, "UTF-8");
    }

    @Override
    public boolean CheckAuthNum(String email, String authNum) {
        String storedAuthNum = redisUtil.getData(email);                // email을 키로 조회
        return storedAuthNum != null && storedAuthNum.equals(authNum);
    }

    @Override
    public boolean isEmailRegistered(String email) {
        return userService.isEmailRegistered(email);
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
            e.printStackTrace();
        }

        // key : 이메일, value : 인증 번호
        redisUtil.setDataExpire(toMail, Integer.toString(authNumber), authCodeExpirationMillis / 60000);
    }
}
