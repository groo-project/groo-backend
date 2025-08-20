package com.x1.groo.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DiscordNotifier {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${discord.webhook-url}")
    private String webhookUrl;

    /**
     * Discord에 에러 메시지 전송
     *
     * @param title   메시지 제목
     * @param message 상세 내용
     */
    public void sendError(String title, String message) {
        try {
            // JSON 안전 처리: 큰따옴표 escape
            String safeMessage = message.replace("\"", "\\\"");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // embed 객체 생성
            Map<String, Object> embed = new HashMap<>();
            embed.put("title", title);
            embed.put("description", message); // \n 포함 가능
            embed.put("color", 16711680);

            Map<String, Object> payload = new HashMap<>();
            payload.put("embeds", new Map[]{embed});

            // 안전하게 JSON 문자열로 변환
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
            restTemplate.postForEntity(webhookUrl, entity, String.class);
        } catch (Exception e) {
            log.error("❌ Discord 전송 실패", e);
        }
    }
}