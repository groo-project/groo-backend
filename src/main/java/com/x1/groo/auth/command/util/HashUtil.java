package com.x1.groo.auth.command.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashUtil {

    public static String sha256(String input) {
        try {
            // 1) SHA-256 해시 알고리즘 인스턴스 획득
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 2) 입력 문자열을 UTF-8 바이트 배열로 변환
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

            // 3) 해시 계산 (256비트 = 32바이트)
            byte[] digest = md.digest(bytes);

            // 4) 바이트 배열을 16진수 문자열로 변환 (소문자)
            String hex = HexFormat.of().formatHex(digest);
            return hex;
        } catch (NoSuchAlgorithmException e) {
            // 이 예외는 일반적으로 발생하지 않지만,
            // 런타임 예외로 감싸서 위로 전달
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
