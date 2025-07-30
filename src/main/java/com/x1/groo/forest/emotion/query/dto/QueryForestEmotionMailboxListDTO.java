package com.x1.groo.forest.emotion.query.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QueryForestEmotionMailboxListDTO {
    private int id;
    private LocalDateTime createdAt;
    private int forestId;
}
