package com.x1.groo.diary.dto;

import lombok.Data;
import java.time.LocalDateTime;


/**
 * 일기 등록 요청 DTO
 */
@Data
public class DiaryRequestDTO {
    private int forestId;
    private String content;
    private int categoryId;
    private LocalDateTime createdAt;
}