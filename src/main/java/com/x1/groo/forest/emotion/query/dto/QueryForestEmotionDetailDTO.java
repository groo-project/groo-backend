package com.x1.groo.forest.emotion.query.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QueryForestEmotionDetailDTO {

    private int forestId;
    private String name;
    private Boolean isPublic;
    private int backgroundId;
    private int userId;

    private String backgroundImageUrl;

    private List<PlacementDTO> placementList = new ArrayList<>();

}
