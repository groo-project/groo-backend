package com.x1.groo.diary.dto;

import com.x1.groo.item.dto.CategoryEmotionItemDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryResponseDTO {
    private int diaryId;
    private int userId;
    private int forestId;
    private Map<String, Integer> topEmotions;
    private String mainEmotion;
    private String weather;
    private String content;

    private List<CategoryEmotionItemDTO> emotionItems;
}