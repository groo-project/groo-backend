package com.x1.groo.forest.mate.query.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MateForestResponseDTO {
    private int forestId;
    private String forestName;

    private int memberCount;

}
