package com.x1.groo.user.dto;

import lombok.Getter;

@Getter
public class UserDTO {
    private String email;
    private String encryptedPwd;
    private String nickname;
    private String role;
}
