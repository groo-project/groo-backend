package com.x1.groo.diary.command.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
public class DiaryDraftListResponseDTO {

    private Integer diaryDraftId;
    private String content;
    private LocalDate diaryDate;
}
