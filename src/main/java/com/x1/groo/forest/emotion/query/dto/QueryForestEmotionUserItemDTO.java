package com.x1.groo.forest.emotion.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QueryForestEmotionUserItemDTO {

    private int id;
    private int totalCount;
    private int placedCount;
    private int itemId;
    private String itemName;
    private int categoryId;
    private String imageUrl;

}
