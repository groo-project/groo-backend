package com.x1.groo.diary.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ResponseDraftedItemsDTO {

    private int id;
    private int diaryId;

    private int itemId1;
    private String itemName1;
    private String itemImageUrl1;

    private int itemId2;
    private String itemName2;
    private String itemImageUrl2;

    private int itemId3;
    private String itemName3;
    private String itemImageUrl3;
}
