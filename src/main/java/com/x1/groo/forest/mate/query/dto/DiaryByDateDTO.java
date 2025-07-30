package com.x1.groo.forest.mate.query.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class DiaryByDateDTO {
    private int diaryId;                // 일기 ID (감정 매핑용)
    private String nickname;            // 일기를 작성한 유저의 닉네임
    private String content;             // 일기의 내용
    private LocalDateTime createdAt;    // 일기 작성 날짜 + 시간
    private List<String> emotions;      // 감정 리스트
}
