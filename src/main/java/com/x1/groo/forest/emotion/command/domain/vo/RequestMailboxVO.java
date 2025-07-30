package com.x1.groo.forest.emotion.command.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RequestMailboxVO {
    private String content;
    private int forestId;
}
