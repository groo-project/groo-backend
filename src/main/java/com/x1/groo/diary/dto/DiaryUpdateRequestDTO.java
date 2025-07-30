package com.x1.groo.diary.dto;

import lombok.Data;

@Data
public class DiaryUpdateRequestDTO {
    private String content;
    private int forestId;
    private int diaryId;
}