package com.x1.groo.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
public class DiaryDraftInfoResponseDTO {
    private Integer draftId;
    private String content;
    private LocalDate diaryDate;
}
