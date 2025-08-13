package com.x1.groo.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoginDTO {
    private String accessToken;
    private String refreshToken;
    private LoginUserDTO user;
    private List<String> roles;

}
