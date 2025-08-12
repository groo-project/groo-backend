package com.x1.groo.forest.emotion.command.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RequestReplantVO {

    private Integer userItemId; // user_item id값

    private BigDecimal itemPositionX;
    private BigDecimal itemPositionY;
    private BigDecimal itemWidth;
    private BigDecimal itemHeight;
    private Integer itemZIndex;
}
