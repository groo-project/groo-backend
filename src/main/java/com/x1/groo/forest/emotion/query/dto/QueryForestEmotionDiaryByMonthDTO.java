package com.x1.groo.forest.emotion.query.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class QueryForestEmotionDiaryByMonthDTO {
    private int diaryId;
    private LocalDateTime createdAt;
}
