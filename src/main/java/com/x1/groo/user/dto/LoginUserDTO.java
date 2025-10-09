package com.x1.groo.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LoginUserDTO {

    @JsonProperty
    private int userId;

    @JsonProperty
    private String email;

    @JsonProperty
    private String nickname;
}
