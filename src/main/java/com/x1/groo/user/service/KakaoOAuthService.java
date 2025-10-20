package com.x1.groo.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.user.dto.KakaoUserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    public KakaoUserInfoDTO getUserInfoByCode(String code) throws Exception {
        // 1) Access Token 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String body = "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&code=" + code
                + "&client_secret=" + clientSecret;
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> tokenRes = restTemplate.exchange(tokenUri, HttpMethod.POST, request, String.class);

        JsonNode json = objectMapper.readTree(tokenRes.getBody());
        String accessToken = json.get("access_token").asText();

        // 2) 사용자 정보 요청
        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> infoRequest = new HttpEntity<>(infoHeaders);
        ResponseEntity<String> infoRes = restTemplate.exchange(userInfoUri, HttpMethod.GET, infoRequest, String.class);

        JsonNode userJson = objectMapper.readTree(infoRes.getBody());
        Long kakaoId = userJson.get("id").asLong();
        String nickname = userJson.get("properties").get("nickname").asText();

        JsonNode emailNode = userJson.path("kakao_account").path("email");
        if (emailNode.isMissingNode() || emailNode.isNull()) {
            throw new CustomException(ErrorCode.USER_EMAIL_INVALID);
        }

        String email = userJson.get("kakao_account").get("email").asText();

        return new KakaoUserInfoDTO(kakaoId, nickname, email);
    }
}
