package com.x1.groo.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.x1.groo.ai.dto.EmotionRequestDTO;
import com.x1.groo.ai.dto.EmotionResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * EmotionService 구현체
 * OpenAI 공식 Java SDK를 사용해 일기 감정 분석을 수행하고 결과를 반환합니다.
 */
@Service
public class EmotionServiceImpl implements EmotionService {

    private final OpenAIClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 감정 → 날씨 매핑
    private static final Map<String, String> EMOTION_MAP = Map.of(
            "즐거움", "맑음",
            "평온함", "맑음",
            "설렘", "꽃비",
            "슬픔", "비",
            "짜증", "황사",
            "불안함", "번개",
            "지침", "흐림",
            "우울함", "안개"
    );

    public EmotionServiceImpl(@Value("${openai.api-key}") String apiKey) {
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    @Override
    public EmotionResponseDTO analyzeEmotion(EmotionRequestDTO request) {
        String diary = request.getDiary();
        int seed = stringToSeed(diary);

        // Chat Completion 요청 파라미터 생성
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4)
                .addUserMessage(buildPrompt(diary))
                .seed(seed)
                .temperature(0.0)
                .build();

        // OpenAI API 호출
        ChatCompletion completion = client.chat().completions().create(params);
        ChatCompletion.Choice choice = completion.choices().get(0);
        String raw = choice.message().content().orElse("").trim();

        // ```json``` 블록 제거
        String json = raw
                .replaceAll("^```(?:json)?\\s*", "")
                .replaceAll("\\s*```$", "");

        // JSON 파싱
        Map<String, Integer> emotions;
        try {
            emotions = objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, Integer>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("감정 분석 JSON 파싱 실패", e);
        }

        // 메인 감정 및 날씨 결정
        String mainEmotion = emotions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow()
                .getKey();
        String weather = EMOTION_MAP.get(mainEmotion);

        return new EmotionResponseDTO(emotions, mainEmotion, weather);
    }

    /** 분석용 프롬프트 생성 */
    private String buildPrompt(String diary) {
        return """
            조건:
            1. 감정들의 값의 총합은 반드시 100이어야 함.
            2. 감정별 백분율은 절대 서로 같지 않아야 함.
            3. 같은 일기에 대해 항상 동일한 결과가 나오도록 일관성 유지.
            4. 감정 리스트: ['즐거움','평온함','설렘','슬픔','짜증','불안함','지침','우울함']
            5. 값이 0이어도 반드시 포함.
            6. 출력은 순수 JSON 객체만.

            일기:
            "%s"
            """.formatted(diary);
    }

    /** 문자열 기반 재현 가능한 seed 생성 */
    private int stringToSeed(String str) {
        long hash = 0;
        for (char c : str.toCharArray()) {
            hash = (hash * 31 + c) % 1_000_000_007;
        }
        return (int) hash;
    }
}
