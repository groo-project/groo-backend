package com.x1.groo.email.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, String> redisTemplate;

    // 데이터 조회
    public String getData(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    // 데이터 저장
    public void setData(String key, String value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value); // key는 이메일, value는 인증코드
    }

    // 일정 시간 후 만료되는 데이터 저장
    public void setDataExpire(String key, String value,
                              long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofMinutes(duration);
        valueOperations.set(key, value, expireDuration);
    }

    // 데이터 삭제
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(@Email @NotEmpty(message = "이메일을 입력해 주세요") String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(email));
    }
}
