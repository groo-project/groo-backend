package com.x1.groo.security.vo;

import com.x1.groo.user.aggregate.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LoginResponseVO {
    private int id;
    private String email;
    private String nickName;

    public static LoginResponseVO of(UserEntity userEntity) {
        return new LoginResponseVO();
    }
}
