package com.x1.groo.diary.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class DiaryUpdateResponseDTO {
    private int diaryId;
    private int userId;
    private int forestId;
    private String content;
    private LocalDateTime updatedAt;
}