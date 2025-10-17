package com.x1.groo.diary.command.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class DiaryDraftUpdateRequestDTO {

    private String content;
    private int diaryDraftId;
}
