package com.x1.groo.user.vo;

import lombok.Getter;

@Getter
public class ResetPasswordRequestVO {

    private String currentPassword;

    private String newPassword;
}
