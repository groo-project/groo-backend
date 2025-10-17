package com.x1.groo.diary.command.domain.dto;

import lombok.Data;

@Data
public class DiaryUpdateRequestDTO {
    private String content;
    private int forestId;
    private int diaryId;
}