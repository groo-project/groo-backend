package com.x1.groo.diary.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ResponseSharedDiaryDetailDTO {
    private int diaryId;                // 일기 ID (감정 매핑용)
    private String content;             // 일기의 내용
    private LocalDateTime createdAt;    // 일기 작성 날짜 + 시간
    private Integer userId;             // 작성자 아이디
    private String nickname;            // 작성자 닉네임
    private List<String> emotions;      // 감정 리스트
    private boolean isItemSelected;     // 아이템 선택 여부
}
