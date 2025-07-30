package com.x1.groo.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiarySaveInfoDTO {
    private int diaryId;
    private LocalDateTime createdAt;
}