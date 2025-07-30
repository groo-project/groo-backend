package com.x1.groo.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEmotionItemDTO {
    private int id;
    private String name;
    private String imageUrl;
    private int categoryId;
    private String emotion;
}
