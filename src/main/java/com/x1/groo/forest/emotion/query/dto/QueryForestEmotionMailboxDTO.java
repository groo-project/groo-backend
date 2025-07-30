package com.x1.groo.forest.emotion.query.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QueryForestEmotionMailboxDTO {

    private int id;
    private String content;
    private LocalDateTime createdAt;
    private int forestId;
    private int userId;

}
