package com.x1.groo.security.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TokenRequestVO {
    private String accessToken;
    private String refreshToken;
}
