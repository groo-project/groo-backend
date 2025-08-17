package com.x1.groo.auth.command.application.dto;


import lombok.Getter;

@Getter
public class RefreshDTO {
    private String refreshToken;

    public RefreshDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
