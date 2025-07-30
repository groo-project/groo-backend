package com.x1.groo.user.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SignupResponseVO {
    private String email;
    private String name;
    private String userId;
}
