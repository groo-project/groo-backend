package com.x1.groo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoTokenResponseDTO {

    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private int expiresIn;
    private int refreshTokenExpiresIn;
}
