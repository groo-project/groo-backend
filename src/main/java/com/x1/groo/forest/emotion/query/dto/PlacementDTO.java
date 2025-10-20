package com.x1.groo.forest.emotion.query.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlacementDTO {

    private int itemId;
    private String itemName;
    private String itemImageUrl;

    private int forestItemId;
    private int forestItemPlacedCount;

    private int placementId;
    private BigDecimal placementPositionX;
    private BigDecimal placementPositionY;
    private BigDecimal placementWidth;
    private BigDecimal placementHeight;
    private Integer placementZIndex;

}
