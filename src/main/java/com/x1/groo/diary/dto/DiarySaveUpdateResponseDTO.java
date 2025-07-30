package com.x1.groo.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 임시 저장된 일기 수정 DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiarySaveUpdateResponseDTO {
    private String content;
    private LocalDateTime updatedAt;
}