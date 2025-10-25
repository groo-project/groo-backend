package com.x1.groo.diary.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class ResponseSharedDiaryListDTO {
    private int diaryId;
    private int userId;
    private LocalDateTime createdAt;
    private boolean isItemSelected;
}
