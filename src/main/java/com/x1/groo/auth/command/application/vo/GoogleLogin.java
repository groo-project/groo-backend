package com.x1.groo.auth.command.application.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GoogleLogin {

    private final int userId;
    private final String nickname;
    private final String accessToken;

}
