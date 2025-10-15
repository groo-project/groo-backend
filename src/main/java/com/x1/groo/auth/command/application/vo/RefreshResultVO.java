package com.x1.groo.auth.command.application.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RefreshResultVO {

    private final String accessToken;
    private final String newRefreshToken;
    private final java.time.Duration refreshTtl;


}
