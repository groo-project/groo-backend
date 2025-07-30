package com.x1.groo.forest.mate.query.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class DiaryByMonthDTO {
    private int diaryId;
    private LocalDateTime createdAt;
}
