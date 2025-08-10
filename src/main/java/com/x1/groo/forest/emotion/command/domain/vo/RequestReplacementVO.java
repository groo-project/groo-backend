package com.x1.groo.forest.emotion.command.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RequestReplacementVO {
    private BigDecimal itemPositionX;
    private BigDecimal itemPositionY;
    private BigDecimal itemWidth;
    private BigDecimal itemHeight;
    private Integer itemZIndex;
    private int placementId;

    public boolean isValid() {
        return itemPositionX != null &&
                itemPositionY != null &&
                itemWidth != null &&
                itemHeight != null &&
                itemZIndex != null &&
                placementId > 0;
    }
}
