package com.x1.groo.forest.emotion.command.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RequestPlacementVO {
    private int forestId;
    private BigDecimal itemPositionX;
    private BigDecimal itemPositionY;
    private BigDecimal itemWidth;
    private BigDecimal itemHeight;
    private int itemZIndex;
    private int itemId;
}
