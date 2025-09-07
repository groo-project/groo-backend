package com.x1.groo.diary.service;

import com.x1.groo.ai.dto.EmotionRequestDTO;
import com.x1.groo.ai.dto.EmotionResponseDTO;
import com.x1.groo.ai.service.EmotionService;
import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.diary.dto.*;
import com.x1.groo.diary.entity.Diary;
import com.x1.groo.diary.entity.DiaryEmotion;
import com.x1.groo.diary.repository.DiaryEmotionRepository;
import com.x1.groo.diary.repository.DiaryRepository;
import com.x1.groo.forest.common.domain.repository.ForestRepository;
import com.x1.groo.forest.emotion.command.domain.repository.EmotionSharedForestRepository;
import com.x1.groo.item.dto.CategoryEmotionItemDTO;
import com.x1.groo.item.service.ItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;


import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private static final int MAX_DIARY_CONTENT_LENGTH = 1050;
    private static final int DIARY_WRITE_LIMIT_DAYS = 2;

    private final DiaryRepository diaryRepo;
    private final DiaryEmotionRepository emotionRepo;
    private final EmotionService emotionService;
    private final ForestRepository forestRepo;
    private final EmotionSharedForestRepository sharedForestRepo;
    private final ItemService itemService;

    @Override
    @Transactional
    public DiaryResponseDTO createDiary(DiaryRequestDTO req, int userId) {

        int forestId = req.getForestId();
        int categoryId = req.getCategoryId();
        LocalDateTime createdAt = req.getCreatedAt();

        LocalDate createdDate = createdAt.toLocalDate();

        // 유효성 검사
        validateDiaryLength(req.getContent());
        validateDiaryDate(createdDate);
        validateDiaryPermission(userId, forestId);

        ///   [실제 사용 기능   ///
//        // AI 감정 분석
//        EmotionResponseDTO aiRes = emotionService.analyzeEmotion(
//                new EmotionRequestDTO(req.getContent())
//        );
//        String mainEmotion = aiRes.getMainEmotion()
//                .trim()
//                .replaceAll("[\"\\r\\n]", "");
//
//        String weather = aiRes.getWeather();
//
//        // Diary 저장
//        Diary diary = new Diary();
//        diary.setContent(req.getContent());
//        diary.setIsPublished(true);
//        diary.setUserId(userId);
//        diary.setForestId(forestId);
//        diary.setWeather(weather);
//        diary.setCreatedAt(createdAt);
//        diary.setUpdatedAt(LocalDateTime.now());
//        Diary savedDiary = diaryRepo.save(diary);
//
//        // 상위 2개 감정 추출 및 저장
//        LinkedHashMap<String, Integer> top2 = aiRes.getEmotionResult().entrySet().stream()
//                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
//                .limit(2)
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue,
//                        (oldVal, newVal) -> oldVal,
//                        LinkedHashMap::new
//                ));
//        top2.forEach((emotion, weight) -> {
//            DiaryEmotion de = new DiaryEmotion();
//            de.setDiary(savedDiary);
//            de.setEmotion(emotion);
//            de.setWeight(weight);
//            emotionRepo.save(de);
//        });
//
//        List<CategoryEmotionItemDTO> emotionItems =
//          itemService.findItemsByCategoryAndEmotion(categoryId, mainEmotion);
        ///   실제 사용 기능]   ///

        ///   [테스트용 기능   /// OpenAPI 요금을 사용하지 않기 위함. 실제 사용 기능 부분을 주석처리 후 사용
        String weather = "맑음";
        String mainEmotion = "즐거움";

        // Diary 저장
        Diary diary = new Diary();
        diary.setContent(req.getContent());
        diary.setUserId(userId);
        diary.setForestId(forestId);
        diary.setWeather(weather);
        diary.setCreatedAt(createdAt);
        diary.setUpdatedAt(LocalDateTime.now());
        Diary savedDiary = diaryRepo.save(diary);

        LinkedHashMap<String, Integer> top2 = new LinkedHashMap<>();
        top2.put("즐거움", 60);
        top2.put("설렘", 40);

        top2.forEach((emotion, weight) -> {
            DiaryEmotion de = new DiaryEmotion();
            de.setDiary(savedDiary);
            de.setEmotion(emotion);
            de.setWeight(weight);
            emotionRepo.save(de);
        });

        List<CategoryEmotionItemDTO> emotionItems =
                itemService.findItemsByCategoryAndEmotion(categoryId, mainEmotion);
        ///   테스트용 기능]   ///

        return new DiaryResponseDTO(
                savedDiary.getId(),
                userId,
                forestId,
                top2,
                mainEmotion,
                weather,
                req.getContent(),
                emotionItems
        );
    }

    // 일기 길이 제한
    private void validateDiaryLength(String content) {
        if (content != null && content.length() >= MAX_DIARY_CONTENT_LENGTH) {
            throw new CustomException(ErrorCode.DIARY_LENGTH_EXCEEDED);
        }
    }

    // 작성 가능 날짜 (2일전 ~ 오늘까지 허용)
    private void validateDiaryDate(LocalDate createdDate) {
        LocalDate today = LocalDate.now();
        LocalDate twoDaysAgo = today.minusDays(DIARY_WRITE_LIMIT_DAYS);

        if (createdDate.isBefore(twoDaysAgo) || createdDate.isAfter(today)) {
            throw new CustomException(ErrorCode.DIARY_WRITE_DATE_NOW_ALLOWED);
        }
    }

    // 권한 검사 (소유자 혹은 공유 사용자만 허용)
    private void validateDiaryPermission(int userId, int forestId) {
        boolean owner = forestRepo.findById(forestId)
                .map(f -> f.getUser().getId() == userId)
                .orElse(false);

        boolean shared = sharedForestRepo.existsByUserIdAndForestId(userId, forestId);

        if (!(owner || shared)) {
            throw new CustomException(ErrorCode.DIARY_ACCESS_DENIED);
        }
    }

    // 일기 수정
    @Override
    @Transactional
    public DiaryUpdateResponseDTO updateDiary(DiaryUpdateRequestDTO req, int userId) {
        int diaryId = req.getDiaryId();
        int forestId = req.getForestId();
        String content = req.getContent();

        // 1. 일기 조회
        Diary diary = diaryRepo.findById(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

        // 2. 권한 체크
        boolean owner = forestRepo.findById(forestId)
                .map(f -> f.getUser().getId() == userId)
                .orElse(false);
        boolean shared = sharedForestRepo.existsByUserIdAndForestId(userId, forestId);

        if (!(owner || shared)) {
            throw new CustomException(ErrorCode.DIARY_ACCESS_DENIED);
        }

        // 3. 내용 수정
        diary.setContent(content);

        // 4. 저장
        diaryRepo.save(diary);

        // 5. 결과 반환
        DiaryUpdateResponseDTO res = new DiaryUpdateResponseDTO();
        res.setDiaryId(diary.getId());
        res.setUserId(diary.getUserId());
        res.setForestId(diary.getForestId());
        res.setContent(diary.getContent());
        res.setUpdatedAt(diary.getUpdatedAt());

        return res;
    }
}