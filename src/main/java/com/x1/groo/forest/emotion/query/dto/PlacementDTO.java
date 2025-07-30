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

    private int userItemId;
    private int userItemPlacedCount;

    private int placementId;
    private BigDecimal placementPositionX;
    private BigDecimal placementPositionY;

}
