package com.x1.groo.forest.mate.query.dto;

import com.x1.groo.forest.emotion.query.dto.PlacementDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MateForestDetailDTO {
    private int forestId;
    private String name;
    private Boolean isPublic;
    private int backgroundId;
    private int userId;

    private String backgroundImageUrl;

    private List<PlacementDTO> placementList = new ArrayList<>();

    private List<String> nicknames = new ArrayList<>();
}
