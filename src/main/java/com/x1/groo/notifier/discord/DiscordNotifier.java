package com.x1.groo.notifier.discord;

import com.x1.groo.notifier.discord.dto.DiscordEmbedDTO;
import com.x1.groo.notifier.discord.dto.DiscordPayloadDTO;
import com.x1.groo.notifier.Notifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component("discordNotifier")
@RequiredArgsConstructor
public class DiscordNotifier implements Notifier {

    private final RestTemplate restTemplate;

    @Value("${discord.webhook-url}")
    private String webhookUrl;

    /**
     * Discord에 에러 메시지 전송
     *
     * @param title   메시지 제목
     * @param message 상세 내용
     */
    @Override
    public void sendError(String title, String message) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // embed 객체 생성
            DiscordEmbedDTO embed = new DiscordEmbedDTO(title, message, 16711680);

            // payload 객체 생성
            DiscordPayloadDTO payload = new DiscordPayloadDTO(new DiscordEmbedDTO[]{embed});

            HttpEntity<DiscordPayloadDTO> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, entity, String.class);
        } catch (Exception e) {
            log.error("❌ Discord 전송 실패", e);
        }
    }
}