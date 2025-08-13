package com.x1.groo.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class LoginUserDTO {
    @JsonProperty
    private int id;
    @JsonProperty
    private String email;
}
